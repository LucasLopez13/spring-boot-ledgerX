package com.lucaslopez.LedgerX.domain.transacciones.validadores;

import com.lucaslopez.LedgerX.domain.billeteras.Billetera;
import com.lucaslopez.LedgerX.domain.transacciones.DatosRegistroTransaccion;
import com.lucaslopez.LedgerX.infra.exception.ValidacionException;
import org.springframework.stereotype.Component;

@Component
public class ValidadorDeUsuario implements ValidadorTransaccion {

    public void validar(Billetera origen, Billetera destino, DatosRegistroTransaccion datos) {
        if (origen != null && destino != null) {
            if (origen.getId().equals(destino.getId())) {
                throw new ValidacionException("El usuario origen no puede ser igual al destinatario.");
            }
        }
    }
}
