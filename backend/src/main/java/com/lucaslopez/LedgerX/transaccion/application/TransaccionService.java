package com.lucaslopez.LedgerX.transaccion.application;

import com.lucaslopez.LedgerX.billetera.domain.Billetera;
import com.lucaslopez.LedgerX.billetera.domain.BilleteraRepository;
import com.lucaslopez.LedgerX.auditoria.domain.TipoAccion;
import com.lucaslopez.LedgerX.auditoria.application.AuditoriaService;
import com.lucaslopez.LedgerX.transaccion.domain.*;
import com.lucaslopez.LedgerX.transaccion.domain.validadores.ValidadorTransaccion;
import com.lucaslopez.LedgerX.shared.exception.ValidacionException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    /**
     PATRÓN STRATEGY: EnumMap para búsqueda O(1) de la estrategia a usar según el
     tipo de transacción.
     Esto respeta el principio Open/Closed (SOLID) evitando un enorme bloque de
     if-else o switch al ejecutar transacciones.
     Las estrategias concretas se construyen una sola vez al inyectar el contexto
     de Spring,
     por lo que no castigan el Garbage Collector inicializando clases por cada
     request.
     */
    private final Map<TipoTransaccion, TransaccionStrategy> estrategias = new EnumMap<>(TipoTransaccion.class);

    @Autowired
    public TransaccionService(List<TransaccionStrategy> estrategiasList) {
        estrategiasList.forEach(e -> estrategias.put(e.getTipoTransaccion(), e));
    }

    @Transactional
    public DatosDetalleTransaccion realizarTransaccion(DatosRegistroTransaccion datos, Long usuarioId) {

        Billetera origen = obtenerBilleteraOrigen(datos.tipoTransaccion(), usuarioId);
        Billetera destino = obtenerBilleteraDestino(datos.tipoTransaccion(), datos.cbuDestino(), usuarioId);

        /**
         DEFENSA EN PROFUNDIDAD (Defense in Depth):
         Los `validadores` ya comprueban restricciones a través de IDs, pero añadimos
         una
         capa extra bloqueando transferencias hacia el mismo CBU de origen de manera
         explícita.
         Esto previene abusos del sistema si, por accidente, la lógica de validación
         interna se corrompe en otra capa.
         */
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

    @Transactional
    public DatosDetalleTransaccion otorgarBonoBienvenida(Long usuarioId) {
        Billetera destino = obtenerBilleteraUsuarioConBloqueo(usuarioId);

        if (destino.getSaldo().compareTo(BigDecimal.ZERO) > 0) {
            throw new ValidacionException("El bono solo aplica a billeteras con saldo 0.");
        }

        var historial = transaccionRepository.buscarHistorialPorBilletera(destino.getId(), Pageable.unpaged());
        if (!historial.isEmpty()) {
            throw new ValidacionException("El bono es exclusivo para cuentas nuevas sin transacciones previas.");
        }

        BigDecimal montoBono = new BigDecimal("5000.00");

        /*
        Se deposita directamente en el modelo de dominio para evadir la estrategia de
        Deposito pública
        y su restricción estricta de ROLE_ADMIN. Así, evitamos exponer
        vulnerabilidades donde usuarios
        puedan inyectarse fondos desde el frontend manual.
         */
        destino.depositar(montoBono);

        billeteraRepository.save(destino);

        var datos = new DatosRegistroTransaccion(destino.getCbu(), montoBono, "Bono de bienvenida",
                TipoTransaccion.DEPOSITO);
        var transaccion = crearYGuardarTransaccion(null, destino, datos);

        auditarTransaccion(null, destino, datos, transaccion.getId());

        return new DatosDetalleTransaccion(transaccion);
    }

    /**
     GESTIÓN DE CONCURRENCIA:
     Operación de Dos Pasos para recuperar billetera previniendo
     Deadlocks y Race Conditions.
     1. Busca normalmente (sin Lock) para obtener datos base o verificar su
     existencia.
     2. Recarga (findByIdWithLock) ejecutando SELECT FOR UPDATE. Esto congela la
     fila de la Billetera
     para depósitos o retiros simultáneos masivos.
     */
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
