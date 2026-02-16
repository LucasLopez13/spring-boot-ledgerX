package com.lucaslopez.LedgerX.domain.transacciones;

import com.lucaslopez.LedgerX.domain.billeteras.Billetera;
import com.lucaslopez.LedgerX.domain.usuarios.Rol;
import com.lucaslopez.LedgerX.domain.usuarios.Usuario;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TransaccionRepositoryTest {

    @Autowired
    private TransaccionRepository transaccionRepository;
    @Autowired
    private EntityManager em;

    private Usuario crearUsuario(String nombre) {
        var usuario = Usuario.builder()
                .nombre(nombre)
                .email(nombre + "@test.com")
                .password("123")
                .rol(Rol.USER)
                .activo(true)
                .build();
        em.persist(usuario);
        return usuario;
    }

    private Billetera crearBilletera(Usuario u) {
        var billetera = new Billetera(u, BigDecimal.ZERO);
        em.persist(billetera);
        return billetera;
    }

    private void crearTransaccion(Billetera origen, Billetera destino, LocalDateTime fecha) {
        var t = Transaccion.builder()
                .billeteraOrigen(origen)
                .billeteraDestino(destino)
                .cantidad(BigDecimal.TEN)
                .tipoTransaccion(TipoTransaccion.TRANSFERENCIA)
                .estadoTransaccion(EstadoTransaccion.EXITOSO)
                .createdAt(fecha)
                .detalleTransaccion("Test")
                .build();
        em.persist(t);
    }

    @Test
    @DisplayName("Debe traer historial desde Origen o Destino, Ordenado por fecha DESC")
    void buscarHistorial() {
        var usuario1 = crearUsuario("User1");
        var usuario2 = crearUsuario("User2");
        var usuario3 = crearUsuario("User3");

        var billetera1 = crearBilletera(usuario1);
        var billetera2 = crearBilletera(usuario2);
        var billetera3 = crearBilletera(usuario3);

        crearTransaccion(billetera1, billetera2, LocalDateTime.now().minusHours(2));

        crearTransaccion(billetera2, billetera1, LocalDateTime.now().minusHours(1));

        crearTransaccion(billetera2, billetera3, LocalDateTime.now());

        var resultado = transaccionRepository.buscarHistorialPorBilletera(billetera1.getId(), PageRequest.of(0, 10));

        assertThat(resultado.getTotalElements()).isEqualTo(2);

        assertThat(resultado.getContent().get(0).getCreatedAt())
                .isAfter(resultado.getContent().get(1).getCreatedAt());
    }

    @Test
    @DisplayName("Debe retornar lista vacia si no hay transacciones para la billetera")
    void buscarHistorialVacio() {
        var usuario = crearUsuario("UserSinTransacciones");
        var billetera = crearBilletera(usuario);

        var resultado = transaccionRepository.buscarHistorialPorBilletera(billetera.getId(), PageRequest.of(0, 10));

        assertThat(resultado.getTotalElements()).isZero();
        assertThat(resultado.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Debe paginar correctamente el historial")
    void buscarHistorialConPaginacion() {
        var usuario1 = crearUsuario("User1");
        var usuario2 = crearUsuario("User2");

        var billetera1 = crearBilletera(usuario1);
        var billetera2 = crearBilletera(usuario2);

        // Crear 5 transacciones
        for (int i = 0; i < 5; i++) {
            crearTransaccion(billetera1, billetera2, LocalDateTime.now().minusHours(i));
        }

        // Página 1 (primeras 2)
        var pagina1 = transaccionRepository.buscarHistorialPorBilletera(
                billetera1.getId(),
                PageRequest.of(0, 2));

        assertThat(pagina1.getTotalElements()).isEqualTo(5);
        assertThat(pagina1.getContent()).hasSize(2);
        assertThat(pagina1.getTotalPages()).isEqualTo(3);

        // Página 2 (siguientes 2)
        var pagina2 = transaccionRepository.buscarHistorialPorBilletera(
                billetera1.getId(),
                PageRequest.of(1, 2));

        assertThat(pagina2.getContent()).hasSize(2);
        assertThat(pagina2.getNumber()).isEqualTo(1);
    }
}