package com.lucaslopez.LedgerX.services;

import com.lucaslopez.LedgerX.domain.billeteras.Billetera;
import com.lucaslopez.LedgerX.domain.billeteras.BilleteraRepository;
import com.lucaslopez.LedgerX.domain.transacciones.*;
import com.lucaslopez.LedgerX.domain.transacciones.operaciones.Deposito;
import com.lucaslopez.LedgerX.domain.transacciones.operaciones.Retiro;
import com.lucaslopez.LedgerX.domain.transacciones.operaciones.Transferencia;
import com.lucaslopez.LedgerX.domain.transacciones.validadores.ValidadorTransaccion;
import com.lucaslopez.LedgerX.domain.usuarios.Rol;
import com.lucaslopez.LedgerX.domain.usuarios.Usuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransaccionServiceTest {

        @Mock
        private BilleteraRepository billeteraRepository;
        @Mock
        private TransaccionRepository transaccionRepository;
        @Mock
        private List<ValidadorTransaccion> validadores;
        @Mock
        private AuditoriaService auditoriaService;

        @Spy
        @InjectMocks
        private TransaccionService transaccionService;

        @BeforeEach
        void setUp() {
                // Inyectar estrategias reales
                List<TransaccionStrategy> estrategias = List.of(
                                new Deposito(),
                                new Retiro(),
                                new Transferencia());

                // Llamar al constructor manualmente para inicializar el Map de estrategias
                transaccionService = new TransaccionService(estrategias);

                // Inyectar los mocks
                ReflectionTestUtils.setField(transaccionService, "billeteraRepository", billeteraRepository);
                ReflectionTestUtils.setField(transaccionService, "transaccionRepository", transaccionRepository);
                ReflectionTestUtils.setField(transaccionService, "validadores", validadores);
                ReflectionTestUtils.setField(transaccionService, "auditoriaService", auditoriaService);
        }

        private Usuario crearUsuario(Long id, String nombre) {
                var usuario = Usuario.builder()
                                .nombre(nombre)
                                .email(nombre + "@test.com")
                                .password("123")
                                .rol(Rol.USER)
                                .activo(true)
                                .build();
                ReflectionTestUtils.setField(usuario, "id", id);
                return usuario;
        }

        private Billetera crearBilletera(Long id, Usuario usuario, String saldo) {
                return crearBilletera(id, usuario, saldo, null);
        }

        private Billetera crearBilletera(Long id, Usuario usuario, String saldo, String cbu) {
                var billetera = new Billetera(usuario, new BigDecimal(saldo));
                ReflectionTestUtils.setField(billetera, "id", id);
                if (cbu != null)
                        ReflectionTestUtils.setField(billetera, "cbu", cbu);
                return billetera;
        }

        @Test
        @DisplayName("Debe realizar deposito correctamente")
        void realizarDeposito() {
                // Mockear SecurityContext para simular usuario ADMIN
                Authentication auth = mock(Authentication.class);
                when(auth.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
                SecurityContext securityContext = mock(SecurityContext.class);
                when(securityContext.getAuthentication()).thenReturn(auth);
                SecurityContextHolder.setContext(securityContext);

                var usuario = crearUsuario(1L, "Pepe");
                var billetera = crearBilletera(1L, usuario, "100");

                when(billeteraRepository.findByUsuarioId(1L)).thenReturn(billetera);
                when(billeteraRepository.findByIdWithLock(1L)).thenReturn(Optional.of(billetera));

                DatosRegistroTransaccion datos = new DatosRegistroTransaccion(
                                null,
                                new BigDecimal("500"),
                                "Deposito inicial",
                                TipoTransaccion.DEPOSITO);

                Transaccion transaccionGuardada = Transaccion.builder()
                                .billeteraDestino(billetera)
                                .cantidad(datos.monto())
                                .tipoTransaccion(TipoTransaccion.DEPOSITO)
                                .estadoTransaccion(EstadoTransaccion.EXITOSO)
                                .detalleTransaccion(datos.descripcion())
                                .build();

                when(transaccionRepository.save(any(Transaccion.class))).thenReturn(transaccionGuardada);

                var resultado = transaccionService.realizarTransaccion(datos, 1L);

                assertNotNull(resultado);
                assertThat(billetera.getSaldo()).isEqualByComparingTo(new BigDecimal("600"));
                verify(billeteraRepository).save(billetera);
                verify(transaccionRepository).save(any(Transaccion.class));
        }

        @Test
        @DisplayName("Debe realizar retiro correctamente")
        void realizarRetiro() {
                var usuario = crearUsuario(1L, "Pepe");
                var billetera = crearBilletera(1L, usuario, "1000");

                when(billeteraRepository.findByUsuarioId(1L)).thenReturn(billetera);
                when(billeteraRepository.findByIdWithLock(1L)).thenReturn(Optional.of(billetera));

                DatosRegistroTransaccion datos = new DatosRegistroTransaccion(
                                null,
                                new BigDecimal("200"),
                                "Retiro de efectivo",
                                TipoTransaccion.RETIRO);

                Transaccion transaccionGuardada = Transaccion.builder()
                                .billeteraOrigen(billetera)
                                .cantidad(datos.monto())
                                .tipoTransaccion(TipoTransaccion.RETIRO)
                                .estadoTransaccion(EstadoTransaccion.EXITOSO)
                                .detalleTransaccion(datos.descripcion())
                                .build();

                when(transaccionRepository.save(any(Transaccion.class))).thenReturn(transaccionGuardada);

                var resultado = transaccionService.realizarTransaccion(datos, 1L);

                assertNotNull(resultado);
                assertThat(billetera.getSaldo()).isEqualByComparingTo(new BigDecimal("800"));
                verify(billeteraRepository).save(billetera);
        }

        @Test
        @DisplayName("Debe realizar transferencia correctamente")
        void realizarTransferencia() {
                var usuarioOrigen = crearUsuario(1L, "Pepe");
                var usuarioDestino = crearUsuario(2L, "Juan");

                var billeteraOrigen = crearBilletera(1L, usuarioOrigen, "1000", "1111111111111111111111");
                var billeteraDestino = crearBilletera(2L, usuarioDestino, "500", "2222222222222222222222");

                when(billeteraRepository.findByUsuarioId(1L)).thenReturn(billeteraOrigen);
                when(billeteraRepository.findByIdWithLock(1L)).thenReturn(Optional.of(billeteraOrigen));
                when(billeteraRepository.findByCbuWithLock("2222222222222222222222"))
                                .thenReturn(Optional.of(billeteraDestino));

                DatosRegistroTransaccion datos = new DatosRegistroTransaccion(
                                "2222222222222222222222",
                                new BigDecimal("300"),
                                "Transferencia",
                                TipoTransaccion.TRANSFERENCIA);

                Transaccion transaccionGuardada = Transaccion.builder()
                                .billeteraOrigen(billeteraOrigen)
                                .billeteraDestino(billeteraDestino)
                                .cantidad(datos.monto())
                                .tipoTransaccion(TipoTransaccion.TRANSFERENCIA)
                                .estadoTransaccion(EstadoTransaccion.EXITOSO)
                                .detalleTransaccion(datos.descripcion())
                                .build();

                when(transaccionRepository.save(any(Transaccion.class))).thenReturn(transaccionGuardada);

                var resultado = transaccionService.realizarTransaccion(datos, 1L);

                assertNotNull(resultado);
                assertThat(billeteraOrigen.getSaldo()).isEqualByComparingTo(new BigDecimal("700"));
                assertThat(billeteraDestino.getSaldo()).isEqualByComparingTo(new BigDecimal("800"));
                verify(billeteraRepository, times(2)).save(any(Billetera.class));
        }

        @Test
        @DisplayName("Debe lanzar excepcion si billetera origen no existe")
        void realizarTransaccionBilleteraOrigenNoExiste() {
                when(billeteraRepository.findByUsuarioId(999L)).thenReturn(null);

                DatosRegistroTransaccion datos = new DatosRegistroTransaccion(
                                null,
                                new BigDecimal("100"),
                                "Retiro",
                                TipoTransaccion.RETIRO);

                assertThrows(EntityNotFoundException.class,
                                () -> transaccionService.realizarTransaccion(datos, 999L));

                verify(transaccionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar excepcion si billetera destino no existe en transferencia")
        void realizarTransferenciaBilleteraDestinoNoExiste() {
                var usuario = crearUsuario(1L, "Pepe");
                var billetera = crearBilletera(1L, usuario, "1000", "1111111111111111111111");

                when(billeteraRepository.findByUsuarioId(1L)).thenReturn(billetera);
                when(billeteraRepository.findByIdWithLock(1L)).thenReturn(Optional.of(billetera));
                when(billeteraRepository.findByCbuWithLock("9999999999999999999999")).thenReturn(Optional.empty());

                DatosRegistroTransaccion datos = new DatosRegistroTransaccion(
                                "9999999999999999999999",
                                new BigDecimal("100"),
                                "Transferencia",
                                TipoTransaccion.TRANSFERENCIA);

                assertThrows(EntityNotFoundException.class,
                                () -> transaccionService.realizarTransaccion(datos, 1L));

                verify(transaccionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe consultar historial con paginacion correctamente")
        void consultarHistorial() {
                var usuario = crearUsuario(1L, "Pepe");
                var billetera = crearBilletera(1L, usuario, "1000");

                Transaccion t1 = Transaccion.builder()
                                .billeteraOrigen(billetera)
                                .cantidad(new BigDecimal("100"))
                                .tipoTransaccion(TipoTransaccion.RETIRO)
                                .estadoTransaccion(EstadoTransaccion.EXITOSO)
                                .detalleTransaccion("Test 1")
                                .build();

                Transaccion t2 = Transaccion.builder()
                                .billeteraDestino(billetera)
                                .cantidad(new BigDecimal("200"))
                                .tipoTransaccion(TipoTransaccion.DEPOSITO)
                                .estadoTransaccion(EstadoTransaccion.EXITOSO)
                                .detalleTransaccion("Test 2")
                                .build();

                Page<Transaccion> page = new PageImpl<>(List.of(t1, t2));

                when(billeteraRepository.findById(1L)).thenReturn(Optional.of(billetera));
                when(transaccionRepository.buscarHistorialPorBilletera(eq(1L), any(PageRequest.class)))
                                .thenReturn(page);

                var resultado = transaccionService.consultarHistorial(1L, PageRequest.of(0, 10));

                assertNotNull(resultado);
                assertThat(resultado.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("Debe lanzar excepcion si usuario no existe al consultar historial")
        void consultarHistorialUsuarioNoExiste() {
                when(billeteraRepository.findById(999L)).thenReturn(Optional.empty());

                assertThrows(EntityNotFoundException.class,
                                () -> transaccionService.consultarHistorial(999L, PageRequest.of(0, 10)));
        }

        @Test
        @DisplayName("Debe usar bloqueo pesimista al obtener billeteras")
        void realizarTransaccionUsaBloqueo() {
                var usuario = crearUsuario(1L, "Pepe");
                var billetera = crearBilletera(1L, usuario, "1000");

                when(billeteraRepository.findByUsuarioId(1L)).thenReturn(billetera);
                when(billeteraRepository.findByIdWithLock(1L)).thenReturn(Optional.of(billetera));

                DatosRegistroTransaccion datos = new DatosRegistroTransaccion(
                                null,
                                new BigDecimal("100"),
                                "Retiro",
                                TipoTransaccion.RETIRO);

                Transaccion transaccionGuardada = Transaccion.builder()
                                .billeteraOrigen(billetera)
                                .cantidad(datos.monto())
                                .tipoTransaccion(TipoTransaccion.RETIRO)
                                .estadoTransaccion(EstadoTransaccion.EXITOSO)
                                .detalleTransaccion(datos.descripcion())
                                .build();

                when(transaccionRepository.save(any(Transaccion.class))).thenReturn(transaccionGuardada);

                transaccionService.realizarTransaccion(datos, 1L);

                // Verifica que se usa findByIdWithLock
                verify(billeteraRepository).findByIdWithLock(1L);
        }
}
