package com.lucaslopez.LedgerX.transaccion.domain;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DatosRegistroTransaccion(
        String cbuDestino,
        @NotNull BigDecimal monto,
        String descripcion,
        @NotNull TipoTransaccion tipoTransaccion) {
}
