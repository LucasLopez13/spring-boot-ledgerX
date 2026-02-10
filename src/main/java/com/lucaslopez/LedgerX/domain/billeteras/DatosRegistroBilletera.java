package com.lucaslopez.LedgerX.domain.billeteras;

import com.lucaslopez.LedgerX.domain.usuarios.Usuario;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DatosRegistroBilletera(
        @NotNull Usuario usuario,
        @NotNull BigDecimal saldo
        ) {
}
