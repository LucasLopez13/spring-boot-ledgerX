package com.lucaslopez.LedgerX.transaccion.domain;

import com.lucaslopez.LedgerX.billetera.domain.Billetera;

import java.math.BigDecimal;

public interface TransaccionStrategy {

    TipoTransaccion getTipoTransaccion();

    void ejecutar(Billetera origen, Billetera destino, BigDecimal monto);
}
