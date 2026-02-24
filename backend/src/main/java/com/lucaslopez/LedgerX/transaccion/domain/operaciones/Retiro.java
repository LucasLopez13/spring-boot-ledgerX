package com.lucaslopez.LedgerX.transaccion.domain.operaciones;

import com.lucaslopez.LedgerX.billetera.domain.Billetera;
import com.lucaslopez.LedgerX.billetera.domain.BilleteraRepository;
import com.lucaslopez.LedgerX.transaccion.domain.DatosRegistroTransaccion;
import com.lucaslopez.LedgerX.transaccion.domain.TipoTransaccion;
import com.lucaslopez.LedgerX.transaccion.domain.TransaccionStrategy;
import com.lucaslopez.LedgerX.shared.exception.ValidacionException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class Retiro implements TransaccionStrategy {
    @Override
    public TipoTransaccion getTipoTransaccion() {
        return TipoTransaccion.RETIRO;
    }

    @Override
    public void ejecutar(Billetera origen, Billetera destino, BigDecimal monto) {
        if (origen == null) {
            throw new ValidacionException("Retiro requiere una cuenta origen");
        }
        if (origen.getSaldo().compareTo(monto) < 0) {
            throw new ValidacionException("Saldo insuficiente para retiro");
        }

        origen.debitar(monto);
    }
}
