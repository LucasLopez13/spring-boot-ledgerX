package com.lucaslopez.LedgerX.domain.transacciones.operaciones;

import com.lucaslopez.LedgerX.domain.billeteras.Billetera;
import com.lucaslopez.LedgerX.domain.usuarios.Usuario;
import com.lucaslopez.LedgerX.infra.exception.ValidacionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest
@AutoConfigureMockMvc
class DepositoTest {
    private final Deposito deposito = new Deposito();

    private Billetera crearBilletera(String saldoInicial) {
        return new Billetera(mock(Usuario.class), new BigDecimal(saldoInicial));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Debe sumar la cantidad indicada al saldo de destino si es ADMIN")
    void ejecutarDeposito() {
        var destino = crearBilletera("100");

        deposito.ejecutar(null, destino, new BigDecimal("500"));

        assertEquals(new BigDecimal("600"), destino.getSaldo());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Debe lanzar excepcion si no es ADMIN")
    void ejecutarDepositoSinPermiso() {
        var destino = crearBilletera("100");

        assertThrows(ValidacionException.class, () -> deposito.ejecutar(null, destino, new BigDecimal("500")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Debe lanzar excepcion si destino es null")
    void verificarDestinoNull() {

        assertThrows(ValidacionException.class, () -> deposito.ejecutar(null, null, new BigDecimal("500")));
    }

}