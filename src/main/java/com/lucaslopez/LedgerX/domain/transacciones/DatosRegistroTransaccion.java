package com.lucaslopez.LedgerX.domain.transacciones;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DatosRegistroTransaccion(
                String cbuDestino,
                @NotNull BigDecimal monto,
                @NotBlank String descripcion,
                @NotNull TipoTransaccion tipoTransaccion) {
}
