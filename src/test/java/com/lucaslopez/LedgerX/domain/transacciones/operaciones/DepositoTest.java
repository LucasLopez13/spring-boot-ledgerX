package com.lucaslopez.LedgerX.domain.transacciones.operaciones;

import com.lucaslopez.LedgerX.domain.billeteras.Billetera;
import com.lucaslopez.LedgerX.domain.usuarios.Usuario;
import com.lucaslopez.LedgerX.infra.exception.ValidacionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DepositoTest {

    private final Deposito deposito = new Deposito();

    private Billetera crearBilletera(String saldoInicial) {
        return new Billetera(mock(Usuario.class), new BigDecimal(saldoInicial));
    }

    private void autenticarComo(String rol) {
        var auth = new UsernamePasswordAuthenticationToken(
                "usuario", "password", List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void limpiarSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe sumar la cantidad indicada al saldo de destino si es ADMIN")
    void ejecutarDeposito() {
        autenticarComo("ADMIN");
        var destino = crearBilletera("100");

        deposito.ejecutar(null, destino, new BigDecimal("500"));

        assertEquals(new BigDecimal("600"), destino.getSaldo());
    }

    @Test
    @DisplayName("Debe lanzar excepcion si no es ADMIN")
    void ejecutarDepositoSinPermiso() {
        autenticarComo("USER");
        var destino = crearBilletera("100");

        assertThrows(ValidacionException.class, () -> deposito.ejecutar(null, destino, new BigDecimal("500")));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si destino es null")
    void verificarDestinoNull() {
        autenticarComo("ADMIN");

        assertThrows(ValidacionException.class, () -> deposito.ejecutar(null, null, new BigDecimal("500")));
    }
}