package com.lucaslopez.LedgerX.domain.transacciones.operaciones;

import com.lucaslopez.LedgerX.domain.billeteras.Billetera;
import com.lucaslopez.LedgerX.domain.billeteras.BilleteraRepository;
import com.lucaslopez.LedgerX.domain.transacciones.DatosRegistroTransaccion;
import com.lucaslopez.LedgerX.domain.transacciones.TipoTransaccion;
import com.lucaslopez.LedgerX.domain.transacciones.TransaccionStrategy;
import com.lucaslopez.LedgerX.infra.exception.ValidacionException;
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
