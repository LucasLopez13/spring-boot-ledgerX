package com.lucaslopez.LedgerX.domain.transacciones;

import com.lucaslopez.LedgerX.domain.billeteras.Billetera;
import com.lucaslopez.LedgerX.domain.billeteras.BilleteraRepository;
import com.lucaslopez.LedgerX.infra.exception.ValidacionException;
import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;

public interface TransaccionStrategy {

    TipoTransaccion getTipoTransaccion();

    void ejecutar(Billetera origen, Billetera destino, BigDecimal monto);
}
