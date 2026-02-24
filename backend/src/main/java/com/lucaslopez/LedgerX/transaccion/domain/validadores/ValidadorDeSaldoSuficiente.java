package com.lucaslopez.LedgerX.transaccion.domain.validadores;

import com.lucaslopez.LedgerX.billetera.domain.Billetera;
import com.lucaslopez.LedgerX.transaccion.domain.DatosRegistroTransaccion;
import com.lucaslopez.LedgerX.shared.exception.ValidacionException;
import org.springframework.stereotype.Component;

@Component
public class ValidadorDeSaldoSuficiente implements ValidadorTransaccion{

    @Override
    public void validar(Billetera origen, Billetera destino, DatosRegistroTransaccion datos) {
        if (origen != null) {
            if (origen.getSaldo().compareTo(datos.monto()) < 0) {
                throw new ValidacionException("Saldo insuficiente para realizar la operación");
            }
        }
    }
}
