package com.lucaslopez.LedgerX.transaccion.domain.operaciones;

import com.lucaslopez.LedgerX.billetera.domain.Billetera;
import com.lucaslopez.LedgerX.transaccion.domain.TipoTransaccion;
import com.lucaslopez.LedgerX.transaccion.domain.TransaccionStrategy;
import com.lucaslopez.LedgerX.shared.exception.ValidacionException;
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
