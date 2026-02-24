package com.lucaslopez.LedgerX.billetera.application;

import com.lucaslopez.LedgerX.billetera.domain.Billetera;
import com.lucaslopez.LedgerX.billetera.domain.BilleteraRepository;
import com.lucaslopez.LedgerX.usuario.domain.Rol;
import com.lucaslopez.LedgerX.usuario.domain.Usuario;
import com.lucaslopez.LedgerX.auditoria.application.AuditoriaService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BilleteraServiceTest {

    @InjectMocks
    private BilleteraService billeteraService;
    @Mock
    private BilleteraRepository repository;
    @Mock
    private AuditoriaService auditoriaService;

    private Usuario crearUsuarioMock() {
        return Usuario.builder()
                .nombre("Test")
                .email("test@test.com")
                .password("123")
                .rol(Rol.USER)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("Debe crear billetera inicial con saldo cero")
    void crearBilleteraInicial() {
        var usuario = crearUsuarioMock();

        billeteraService.crearBilleteraInicial(usuario);

        verify(repository).save(argThat(billetera -> billetera.getSaldo().compareTo(BigDecimal.ZERO) == 0 &&
                billetera.getUsuario().equals(usuario)));
    }

    @Test
    @DisplayName("Debe consultar saldo correctamente y auditar")
    void consultarSaldo() {
        var usuario = crearUsuarioMock();
        var billetera = new Billetera(usuario, new BigDecimal("1000"));

        when(repository.findByUsuarioId(1L)).thenReturn(billetera);

        var resultado = billeteraService.consultarSaldo(1L);

        assertNotNull(resultado);
        assertThat(resultado.saldo()).isEqualByComparingTo(new BigDecimal("1000"));
        verify(repository).findByUsuarioId(1L);
        verify(auditoriaService).registrarActividad(eq(1L), any(), any(), any());
    }

    @Test
    @DisplayName("Debe lanzar excepcion si usuario no existe al consultar saldo")
    void consultarSaldoUsuarioNoExiste() {
        when(repository.findByUsuarioId(999L)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> billeteraService.consultarSaldo(999L));

        verify(auditoriaService, never()).registrarActividad(any(), any(), any(), any());
    }
}
