package com.lucaslopez.LedgerX.billetera.domain;

import java.math.BigDecimal;

public record DatosDetalleSaldo(
        Long idBilletera,
        BigDecimal saldo,
        String nombreUsuario,
        String emailUsuario,
        String cbu) {
    public DatosDetalleSaldo(Billetera billetera) {
        this(
                billetera.getId(),
                billetera.getSaldo(),
                billetera.getUsuario().getNombre(),
                billetera.getUsuario().getEmail(),
                billetera.getCbu());
    }
}
