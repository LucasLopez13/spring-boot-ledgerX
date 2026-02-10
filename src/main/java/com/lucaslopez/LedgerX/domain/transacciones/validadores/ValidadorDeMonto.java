package com.lucaslopez.LedgerX.domain.transacciones.validadores;

import com.lucaslopez.LedgerX.domain.billeteras.Billetera;
import com.lucaslopez.LedgerX.domain.transacciones.DatosRegistroTransaccion;
import com.lucaslopez.LedgerX.infra.exception.ValidacionException;
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
