package com.lucaslopez.LedgerX.domain.transacciones.operaciones;

import com.lucaslopez.LedgerX.domain.billeteras.Billetera;
import com.lucaslopez.LedgerX.domain.transacciones.TipoTransaccion;
import com.lucaslopez.LedgerX.domain.transacciones.TransaccionStrategy;
import com.lucaslopez.LedgerX.infra.exception.ValidacionException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class Deposito implements TransaccionStrategy {
    @Override
    public TipoTransaccion getTipoTransaccion() {
        return TipoTransaccion.DEPOSITO;
    }

    @Override
    public void ejecutar(Billetera origen, Billetera destino, BigDecimal monto) {

        var auth = SecurityContextHolder.getContext().getAuthentication();

        var esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!esAdmin) {
            throw new ValidacionException("No estas autorizado para realizar esta operacion");
        }

        if (destino == null) {
            throw new ValidacionException("Depósito requiere una cuenta destino");
        }

        destino.depositar(monto);
    }
}
