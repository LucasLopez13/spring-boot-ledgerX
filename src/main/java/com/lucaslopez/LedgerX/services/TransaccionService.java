package com.lucaslopez.LedgerX.services;

import com.lucaslopez.LedgerX.domain.billeteras.Billetera;
import com.lucaslopez.LedgerX.domain.billeteras.BilleteraRepository;
import com.lucaslopez.LedgerX.domain.logActivitys.TipoAccion;
import com.lucaslopez.LedgerX.domain.transacciones.*;
import com.lucaslopez.LedgerX.domain.transacciones.validadores.ValidadorTransaccion;
import com.lucaslopez.LedgerX.infra.exception.ValidacionException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class TransaccionService {

    @Autowired
    private BilleteraRepository billeteraRepository;
    @Autowired
    private TransaccionRepository transaccionRepository;
    @Autowired
    private List<ValidadorTransaccion> validadores;
    @Autowired
    private AuditoriaService auditoriaService;

    private final Map<TipoTransaccion, TransaccionStrategy> estrategias = new EnumMap<>(TipoTransaccion.class);

    @Autowired
    public TransaccionService(List<TransaccionStrategy> estrategiasList) {
        estrategiasList.forEach(e -> estrategias.put(e.getTipoTransaccion(), e));
    }

    @Transactional
    public DatosDetalleTransaccion realizarTransaccion(DatosRegistroTransaccion datos, Long usuarioId) {

        Billetera origen = obtenerBilleteraOrigen(datos.tipoTransaccion(), usuarioId);
        Billetera destino = obtenerBilleteraDestino(datos.tipoTransaccion(), datos.idCuentaDestino(), usuarioId);

        validadores.forEach(v -> v.validar(origen, destino, datos));

        TransaccionStrategy estrategia = estrategias.get(datos.tipoTransaccion());
        if (estrategia == null) {
            throw new ValidacionException("Tipo de transacción no soportada: " + datos.tipoTransaccion());
        }

        estrategia.ejecutar(origen, destino, datos.monto());

        if (origen != null)
            billeteraRepository.save(origen);
        if (destino != null)
            billeteraRepository.save(destino);

        var transaccion = crearYGuardarTransaccion(origen, destino, datos);

        auditarTransaccion(origen, destino, datos, transaccion.getId());

        return new DatosDetalleTransaccion(transaccion);
    }

    private Billetera obtenerBilleteraOrigen(TipoTransaccion tipo, Long usuarioId) {
        return switch (tipo) {
            case RETIRO, TRANSFERENCIA -> obtenerBilleteraUsuarioConBloqueo(usuarioId);
            case DEPOSITO -> null;
        };
    }

    private Billetera obtenerBilleteraDestino(TipoTransaccion tipo, Long idCuentaDestino, Long usuarioId) {
        return switch (tipo) {
            case DEPOSITO -> obtenerBilleteraUsuarioConBloqueo(usuarioId);
            case TRANSFERENCIA -> buscarBilleteraConBloqueo(idCuentaDestino);
            case RETIRO -> null;
        };
    }

    private Transaccion crearYGuardarTransaccion(Billetera origen, Billetera destino, DatosRegistroTransaccion datos) {
        var transaccion = Transaccion.builder()
                .billeteraOrigen(origen)
                .billeteraDestino(destino)
                .cantidad(datos.monto())
                .detalleTransaccion(datos.descripcion())
                .tipoTransaccion(datos.tipoTransaccion())
                .estadoTransaccion(EstadoTransaccion.EXITOSO)
                .createdAt(LocalDateTime.now())
                .build();

        return transaccionRepository.save(transaccion);
    }

    private void auditarTransaccion(Billetera origen, Billetera destino, DatosRegistroTransaccion datos,
            Long transaccionId) {
        TipoAccion tipoLog = switch (datos.tipoTransaccion()) {
            case DEPOSITO -> TipoAccion.DEPOSITO_REALIZADO;
            case RETIRO -> TipoAccion.RETIRO_REALIZADO;
            case TRANSFERENCIA -> TipoAccion.TRANSFERENCIA_REALIZADA;
        };

        Long idAutor = (origen != null) ? origen.getUsuario().getId() : destino.getUsuario().getId();

        auditoriaService.registrarActividad(
                idAutor,
                tipoLog,
                "Monto: " + datos.monto() + " | Ref: " + transaccionId,
                "127.0.0.1");
    }

    public Page<DatosDetalleTransaccion> consultarHistorial(Long usuarioId, Pageable pageable) {
        var billeteraUsuario = billeteraRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        return transaccionRepository.buscarHistorialPorBilletera(billeteraUsuario.getId(), pageable)
                .map(DatosDetalleTransaccion::new);
    }

    private Billetera obtenerBilleteraUsuarioConBloqueo(Long usuarioId) {
        Billetera billetera = billeteraRepository.findByUsuarioId(usuarioId);

        if (billetera == null) {
            throw new EntityNotFoundException("No se encontró billetera para el usuario ID: " + usuarioId);
        }

        return billeteraRepository.findByIdWithLock(billetera.getId())
                .orElseThrow(() -> new EntityNotFoundException("Error al bloquear billetera ID: " + billetera.getId()));
    }

    private Billetera buscarBilleteraConBloqueo(Long id) {
        return billeteraRepository.findByIdWithLock(id)
                .orElseThrow(() -> new EntityNotFoundException("Billetera no encontrada ID: " + id));
    }

}
