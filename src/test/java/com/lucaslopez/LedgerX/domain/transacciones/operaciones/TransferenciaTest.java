package com.lucaslopez.LedgerX.domain.transacciones.operaciones;

import com.lucaslopez.LedgerX.domain.billeteras.Billetera;
import com.lucaslopez.LedgerX.domain.usuarios.Usuario;
import com.lucaslopez.LedgerX.infra.exception.ValidacionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class TransferenciaTest {

    private final Transferencia transaccion = new Transferencia();

    private Billetera crearBilletera(String saldoInicial) {
        return new Billetera(mock(Usuario.class),new BigDecimal(saldoInicial));
    }

    @Test
    @DisplayName("Debe descontar saldo del origen y aumentar al destino")
    void ejecutarTransferencia() {
        var origen = crearBilletera("1000");
        var destino = crearBilletera("500");

        transaccion.ejecutar(origen,destino, new BigDecimal("200"));

        assertEquals(new BigDecimal("800"),origen.getSaldo());
        assertEquals(new BigDecimal("700"),destino.getSaldo());
    }

    @Test
    @DisplayName("Debe lanzar excpecion si origen es null")
    void ejecutarTransferenciaOrigenNull() {
        var destino = crearBilletera("500");

        assertThrows(ValidacionException.class,() -> transaccion.ejecutar(null,destino, new BigDecimal("200")));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si destino es null")
    void ejecutarTransferenciaDestinoNull() {
        var origen = crearBilletera("1000");

        assertThrows(ValidacionException.class,() -> transaccion.ejecutar(origen,null, new BigDecimal("200")));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si saldo es insuficiente")
    void ejecutarTransferenciaSaldoInsuficiente() {
        var origen = crearBilletera("300");
        var destino = crearBilletera("500");

        assertThrows(ValidacionException.class,() -> transaccion.ejecutar(origen,destino, new BigDecimal("400")));
    }
}