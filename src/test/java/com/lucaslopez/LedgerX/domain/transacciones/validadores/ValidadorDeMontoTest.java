package com.lucaslopez.LedgerX.domain.transacciones.validadores;

import com.lucaslopez.LedgerX.domain.transacciones.DatosRegistroTransaccion;
import com.lucaslopez.LedgerX.domain.transacciones.TipoTransaccion;
import com.lucaslopez.LedgerX.infra.exception.ValidacionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorDeMontoTest {

    private final ValidadorDeMonto validador = new ValidadorDeMonto();

    @Test
    @DisplayName("Debe lanzar excepcion si el monto es igual a 0")
    void validarMontoCero() {
        var datos = new DatosRegistroTransaccion(null, BigDecimal.ZERO, "Test Deposito", TipoTransaccion.DEPOSITO);

        assertThrows(ValidacionException.class, () -> validador.validar(null, null, datos));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si el monto es menor a 0")
    void validarMontoNegativo() {
        var datos = new DatosRegistroTransaccion(null, new BigDecimal("-100"), "Test Deposito",
                TipoTransaccion.DEPOSITO);

        assertThrows(ValidacionException.class, () -> validador.validar(null, null, datos));
    }

    @Test
    @DisplayName("No Debe lanzar excepcion si el monto es mayor a 0")
    void validarMontoPositivo() {
        var datos = new DatosRegistroTransaccion(null, new BigDecimal("100"), "Test Deposito",
                TipoTransaccion.DEPOSITO);

        assertDoesNotThrow(() -> validador.validar(null, null, datos));
    }

}