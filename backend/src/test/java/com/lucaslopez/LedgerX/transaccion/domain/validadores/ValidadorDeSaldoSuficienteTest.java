package com.lucaslopez.LedgerX.transaccion.domain.validadores;

import com.lucaslopez.LedgerX.billetera.domain.Billetera;
import com.lucaslopez.LedgerX.transaccion.domain.DatosRegistroTransaccion;
import com.lucaslopez.LedgerX.transaccion.domain.TipoTransaccion;
import com.lucaslopez.LedgerX.transaccion.domain.validadores.ValidadorDeSaldoSuficiente;
import com.lucaslopez.LedgerX.shared.exception.ValidacionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidadorDeSaldoSuficienteTest {

    private final ValidadorDeSaldoSuficiente validador = new ValidadorDeSaldoSuficiente();

    @Test
    @DisplayName("Debe lanzar excepcion si el saldo es insuficiente")
    void validarSaldoInsuficiente() {
        var origen = mock(Billetera.class);
        when(origen.getSaldo()).thenReturn(new BigDecimal("50"));

        var datos = new DatosRegistroTransaccion(null,new BigDecimal("100"),"Test Retiro", TipoTransaccion.RETIRO);

        assertThrows(ValidacionException.class, () -> validador.validar(origen,null,datos));
    }

    @Test
    @DisplayName("No debe lanzar excepcion si el saldo es suficiente")
    void validarSaldoSuficiente() {
        var origen = mock(Billetera.class);
        when(origen.getSaldo()).thenReturn(new BigDecimal("150"));

        var datos = new DatosRegistroTransaccion(null,new BigDecimal("100"),"Test Retiro", TipoTransaccion.RETIRO);

        assertDoesNotThrow(() -> validador.validar(origen,null,datos));
    }

    @Test
    @DisplayName("No debe validar saldo si es un deposito")
    void validarDeposito() {
        var datos = new DatosRegistroTransaccion(null,new BigDecimal("100"),"Test Desposito", TipoTransaccion.DEPOSITO);

        assertDoesNotThrow(() -> validador.validar(null,null,datos));
    }
}