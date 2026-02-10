package com.lucaslopez.LedgerX.domain.transacciones.validadores;

import com.lucaslopez.LedgerX.domain.billeteras.Billetera;
import com.lucaslopez.LedgerX.domain.transacciones.DatosRegistroTransaccion;

public interface ValidadorTransaccion {
    void validar(Billetera origen, Billetera destino, DatosRegistroTransaccion datos);

}
