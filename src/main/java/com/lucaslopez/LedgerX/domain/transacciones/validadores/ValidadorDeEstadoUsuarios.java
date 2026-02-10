package com.lucaslopez.LedgerX.domain.transacciones.validadores;

import com.lucaslopez.LedgerX.domain.billeteras.Billetera;
import com.lucaslopez.LedgerX.domain.transacciones.DatosRegistroTransaccion;
import com.lucaslopez.LedgerX.infra.exception.ValidacionException;
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
