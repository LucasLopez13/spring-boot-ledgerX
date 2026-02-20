package com.lucaslopez.LedgerX.services;

import com.lucaslopez.LedgerX.domain.idempotency.Idempotencia;
import com.lucaslopez.LedgerX.domain.idempotency.IdempotenciaRepository;
import com.lucaslopez.LedgerX.domain.idempotency.IdempotenciaStatus;
import com.lucaslopez.LedgerX.infra.exception.ConflictException;
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

    /*
       Verifica si una clave de idempotencia ya fue procesada.
       - Si la clave no existe, crea un registro con status PROCESSING y retorna
       Optional.empty()
       - Si la clave ya fue completada, retorna la respuesta almacenada
       - Si la clave está en proceso, lanza ConflictException

        Usa REQUIRES_NEW para que el INSERT se haga en una transacción separada,
        así el constraint UNIQUE funciona como lock distribuido.
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
