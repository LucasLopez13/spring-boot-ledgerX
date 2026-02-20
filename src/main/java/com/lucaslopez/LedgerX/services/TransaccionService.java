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

    // EnumMap para O(1) lookup de la estrategia según tipo de transacción.
    // Se construye una sola vez al iniciar el contexto para no instanciar
    // strategies en cada request.
    private final Map<TipoTransaccion, TransaccionStrategy> estrategias = new EnumMap<>(TipoTransaccion.class);

    @Autowired
    public TransaccionService(List<TransaccionStrategy> estrategiasList) {
        estrategiasList.forEach(e -> estrategias.put(e.getTipoTransaccion(), e));
    }

    @Transactional
    public DatosDetalleTransaccion realizarTransaccion(DatosRegistroTransaccion datos, Long usuarioId) {

        Billetera origen = obtenerBilleteraOrigen(datos.tipoTransaccion(), usuarioId);
        Billetera destino = obtenerBilleteraDestino(datos.tipoTransaccion(), datos.cbuDestino(), usuarioId);

        // Validación extra de auto-transferencia usando CBU (defense in depth).
        // ValidadorDeUsuario ya lo valida por ID, pero verificamos también por CBU
        // por si en el futuro cambiase la lógica de asignación de IDs.
        if (origen != null && destino != null && origen.getCbu().equals(destino.getCbu())) {
            throw new ValidacionException("No puedes transferirte a tu propia billetera");
        }

        validadores.forEach(v -> v.validar(origen, destino, datos));

        TransaccionStrategy estrategia = estrategias.get(datos.tipoTransaccion());
        if (estrategia == null) {
            throw new ValidacionException("Tipo de transacción no soportada: " + datos.tipoTransaccion());
        }

        estrategia.ejecutar(origen, destino, datos.monto());

        // Solo guardamos billeteras que participaron en la operación.
        // En RETIRO, destino es null. En DEPOSITO, origen es null.
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

    private Billetera obtenerBilleteraDestino(TipoTransaccion tipo, String cbuDestino, Long usuarioId) {
        return switch (tipo) {
            case DEPOSITO -> obtenerBilleteraUsuarioConBloqueo(usuarioId);
            case TRANSFERENCIA -> buscarBilleteraConBloqueo(cbuDestino);
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

    // Dos pasos: primero buscamos sin lock para encontrar el ID,
    // luego re-buscamos con PESSIMISTIC_WRITE para bloquear la fila hasta que
    // termine la transacción.
    // Esto evita race conditions en depósitos concurrentes sobre la misma
    // billetera.
    private Billetera obtenerBilleteraUsuarioConBloqueo(Long usuarioId) {
        Billetera billetera = billeteraRepository.findByUsuarioId(usuarioId);

        if (billetera == null) {
            throw new EntityNotFoundException("No se encontró billetera para el usuario ID: " + usuarioId);
        }

        return billeteraRepository.findByIdWithLock(billetera.getId())
                .orElseThrow(() -> new EntityNotFoundException("Error al bloquear billetera ID: " + billetera.getId()));
    }

    private Billetera buscarBilleteraConBloqueo(String cbu) {
        return billeteraRepository.findByCbuWithLock(cbu)
                .orElseThrow(() -> new EntityNotFoundException("Billetera no encontrada con CBU: " + cbu));
    }

}
