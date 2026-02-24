package com.lucaslopez.LedgerX.transaccion.domain.validadores;

import com.lucaslopez.LedgerX.billetera.domain.Billetera;
import com.lucaslopez.LedgerX.transaccion.domain.DatosRegistroTransaccion;

public interface ValidadorTransaccion {
    void validar(Billetera origen, Billetera destino, DatosRegistroTransaccion datos);

}
