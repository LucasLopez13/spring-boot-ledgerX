package com.lucaslopez.LedgerX.domain.billeteras;

import java.math.BigDecimal;

public record DatosDetalleSaldo(
        Long idBilletera,
        BigDecimal saldo,
        String emailUsuario
) {
    public DatosDetalleSaldo(Billetera billetera) {
        this(
                billetera.getId(),
                billetera.getSaldo(),
                billetera.getUsuario().getEmail()
        );
    }
}
