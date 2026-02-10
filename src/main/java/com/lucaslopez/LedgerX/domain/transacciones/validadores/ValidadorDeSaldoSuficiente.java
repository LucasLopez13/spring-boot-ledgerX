package com.lucaslopez.LedgerX.domain.transacciones.validadores;

import com.lucaslopez.LedgerX.domain.billeteras.Billetera;
import com.lucaslopez.LedgerX.domain.transacciones.DatosRegistroTransaccion;
import com.lucaslopez.LedgerX.infra.exception.ValidacionException;
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
