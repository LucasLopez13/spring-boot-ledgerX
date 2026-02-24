package com.lucaslopez.LedgerX.transaccion.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DatosDetalleTransaccion(
        Long idTransaccion,
        Long idCuentaOrigen,
        Long idCuentaDestino,
        BigDecimal monto,
        String descripcion,
        TipoTransaccion tipoTransaccion,
        EstadoTransaccion estadoTransaccion,
        LocalDateTime fechaDeCreacion) {
    public DatosDetalleTransaccion(Transaccion transaccion) {
        this(
                transaccion.getId(),
                (transaccion.getBilleteraOrigen() != null) ? transaccion.getBilleteraOrigen().getId() : null,
                (transaccion.getBilleteraDestino() != null) ? transaccion.getBilleteraDestino().getId() : null,
                transaccion.getCantidad(),
                transaccion.getDetalleTransaccion(),
                transaccion.getTipoTransaccion(),
                transaccion.getEstadoTransaccion(),
                transaccion.getCreatedAt());
    }
}
