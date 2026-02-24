package com.lucaslopez.LedgerX.transaccion.domain.validadores;

import com.lucaslopez.LedgerX.billetera.domain.Billetera;
import com.lucaslopez.LedgerX.transaccion.domain.DatosRegistroTransaccion;
import com.lucaslopez.LedgerX.shared.exception.ValidacionException;
import org.springframework.stereotype.Component;

@Component
public class ValidadorDeEstadoUsuarios implements ValidadorTransaccion{

    @Override
    public void validar(Billetera origen, Billetera destino, DatosRegistroTransaccion datos) {
        validarActivo(origen, "Su usuario se encuentra inactivo y no puede operar.");

        validarActivo(destino, "No se puede transferir a un usuario inactivo.");
    }

    private void validarActivo(Billetera billetera, String mensajeError) {
        if (billetera != null && !billetera.getUsuario().isActivo()) {
            throw new ValidacionException(mensajeError);
        }
    }
}
