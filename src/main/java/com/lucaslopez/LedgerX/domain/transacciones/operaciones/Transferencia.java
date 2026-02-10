package com.lucaslopez.LedgerX.domain.transacciones.operaciones;

import com.lucaslopez.LedgerX.domain.billeteras.Billetera;
import com.lucaslopez.LedgerX.domain.transacciones.TipoTransaccion;
import com.lucaslopez.LedgerX.domain.transacciones.TransaccionStrategy;
import com.lucaslopez.LedgerX.infra.exception.ValidacionException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class Transferencia implements TransaccionStrategy {
    @Override
    public TipoTransaccion getTipoTransaccion() {
        return TipoTransaccion.TRANSFERENCIA;
    }

    @Override
    public void ejecutar(Billetera origen, Billetera destino, BigDecimal monto) {
        if (origen == null || destino == null) {
            throw new ValidacionException("Transferencia requiere origen y destino");
        }
        if (origen.getSaldo().compareTo(monto) < 0) {
            throw new ValidacionException("Saldo insuficiente en origen");
        }
        origen.debitar(monto);
        destino.depositar(monto);
    }
}
