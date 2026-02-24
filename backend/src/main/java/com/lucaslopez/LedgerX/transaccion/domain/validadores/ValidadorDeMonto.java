package com.lucaslopez.LedgerX.transaccion.domain.validadores;

import com.lucaslopez.LedgerX.billetera.domain.Billetera;
import com.lucaslopez.LedgerX.transaccion.domain.DatosRegistroTransaccion;
import com.lucaslopez.LedgerX.shared.exception.ValidacionException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ValidadorDeMonto implements ValidadorTransaccion{

    @Override
    public void validar(Billetera origen, Billetera destino, DatosRegistroTransaccion datos) {
        if (datos.monto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacionException("Monto invalido");
        }
    }
}
