package com.lucaslopez.LedgerX.idempotencia.application;

import com.lucaslopez.LedgerX.idempotencia.domain.Idempotencia;
import com.lucaslopez.LedgerX.idempotencia.domain.IdempotenciaRepository;
import com.lucaslopez.LedgerX.idempotencia.domain.IdempotenciaStatus;
import com.lucaslopez.LedgerX.shared.exception.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class IdempotenciaService {

    @Autowired
    private IdempotenciaRepository idempotenciaRepository;

    /**
     MECANISMO DE IDEMPOTENCIA Y DISTRIBUTED-LOCK:
     Verifica si una clave de idempotencia ya fue procesada.
     REQUIRES_NEW: Es el centro de esta arquitectura.
     Fuerza a que este INSERT se ejecute y comitee en una "transacción DB
     independiente"
     ANTES de continuar con el pago real en TransaccionService.
     Al ser un INSERT independiente, si un usuario da doble clic rápido al botón
     "Pagar",
     el segundo hilo intentará insertar la misma llave y el UNIQUE CONSTRAINT de
     la DB
     lanzará `DataIntegrityViolationException`, funcionando efectivamente como un
     candado sin costo adicional.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<Idempotencia> verificarOCrear(String idempotenciaKey, Long usuarioId) {

        var existente = idempotenciaRepository.findByIdempotenciaKey(idempotenciaKey);

        if (existente.isPresent()) {
            var registro = existente.get();

            if (registro.getStatus() == IdempotenciaStatus.COMPLETED) {
                return Optional.of(registro);
            }

            // Todavía en proceso
            throw new ConflictException(
                    "La transacción con esta clave de idempotencia está siendo procesada. Intente nuevamente.");
        }

        // Clave nueva,registrar como PROCESSING
        try {
            var nuevoRegistro = Idempotencia.builder()
                    .idempotenciaKey(idempotenciaKey)
                    .usuarioId(usuarioId)
                    .status(IdempotenciaStatus.PROCESSING)
                    .build();

            idempotenciaRepository.save(nuevoRegistro);
            return Optional.empty();

        } catch (DataIntegrityViolationException e) {
            // Condición de carrera: otro request insertó la misma clave justo ahora
            throw new ConflictException(
                    "La transacción con esta clave de idempotencia está siendo procesada. Intente nuevamente.");
        }
    }

    /*
     Marca una clave de idempotencia como completada y almacena la respuesta.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completar(String idempotenciaKey, int httpStatus, String responseBody) {
        var registro = idempotenciaRepository.findByIdempotenciaKey(idempotenciaKey)
                .orElseThrow(() -> new IllegalStateException(
                        "Registro de idempotencia no encontrado para la clave: " + idempotenciaKey));

        registro.setStatus(IdempotenciaStatus.COMPLETED);
        registro.setResponseStatus(httpStatus);
        registro.setResponseBody(responseBody);

        idempotenciaRepository.save(registro);
    }
}
