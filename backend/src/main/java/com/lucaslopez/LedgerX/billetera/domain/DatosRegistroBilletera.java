package com.lucaslopez.LedgerX.billetera.domain;

import com.lucaslopez.LedgerX.usuario.domain.Usuario;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DatosRegistroBilletera(
        @NotNull Usuario usuario,
        @NotNull BigDecimal saldo
        ) {
}
