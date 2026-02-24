package com.lucaslopez.LedgerX.transaccion.domain.operaciones;

import com.lucaslopez.LedgerX.billetera.domain.Billetera;
import com.lucaslopez.LedgerX.usuario.domain.Usuario;
import com.lucaslopez.LedgerX.shared.exception.ValidacionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RetiroTest {

    private final Retiro retiro = new Retiro();

    private Billetera crearBilletera(String saldoInicial) {
        return new Billetera(mock(Usuario.class),new BigDecimal(saldoInicial));
    }

    @Test
    @DisplayName("Debe descontar el monto a retirar de Origen")
    void ejecutarRetiro() {
        var origen = crearBilletera("1000");

        var monto = new BigDecimal("500");

        retiro.ejecutar(origen,null,monto);

        assertEquals(new BigDecimal("500"), origen.getSaldo());
    }

    @Test
    @DisplayName("Debe lanzar excepcion si Origen es null")
    void verificarOrigenNull() {

        assertThrows(ValidacionException.class, () -> retiro.ejecutar(null,null,new BigDecimal("500")));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si saldo es insuficiente")
    void verificarSaldoInsuficiente() {
        var origen = crearBilletera("100");

        assertThrows(ValidacionException.class, () -> retiro.ejecutar(origen,null,new BigDecimal("500")));
    }
}