package com.lucaslopez.LedgerX.transaccion.infrastructure;

import com.lucaslopez.LedgerX.transaccion.domain.DatosRegistroTransaccion;
import com.lucaslopez.LedgerX.transaccion.domain.DatosDetalleTransaccion;
import com.lucaslopez.LedgerX.usuario.domain.Usuario;
import com.lucaslopez.LedgerX.idempotencia.application.IdempotenciaService;
import com.lucaslopez.LedgerX.transaccion.application.TransaccionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/transacciones")
@SecurityRequirement(name = "bearer-jwt")
public class TransaccionController {

    @Autowired
    private TransaccionService transaccionService;

    @Autowired
    private IdempotenciaService idempotenciaService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity realizarTransaccion(
            @RequestHeader(value = "Idempotencia-Key", required = false) String idempotenciaKey,
            @RequestBody @Valid DatosRegistroTransaccion datos,
            UriComponentsBuilder uriBuilder) {

        // Si el cliente no envía el header, generamos uno interno para mantener la
        // consistencia
        if (idempotenciaKey == null || idempotenciaKey.isBlank()) {
            idempotenciaKey = java.util.UUID.randomUUID().toString();
        }

        var usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 1. REVISION DE IDEMPOTENCIA
        // Previene la duplicación de extracciones/transferencias si el usuario
        // experimenta lag
        // en la red o presiona múltiples veces "Confirmar".
        var registroExistente = idempotenciaService.verificarOCrear(idempotenciaKey, usuario.getId());

        if (registroExistente.isPresent()) {
            var registro = registroExistente.get();
            try {
                var respuestaOriginal = objectMapper.readValue(
                        registro.getResponseBody(), DatosDetalleTransaccion.class);
                return ResponseEntity.status(registro.getResponseStatus()).body(respuestaOriginal);
            } catch (Exception e) {
                return ResponseEntity.status(registro.getResponseStatus()).body(registro.getResponseBody());
            }
        }

        // 2. Ejecutar transacción
        var transaccion = transaccionService.realizarTransaccion(datos, usuario.getId());

        // 3. CACHING DE RESPUESTA
        // Se guarda el transaccion en JSON para evitar volver a ejecutar el servicio si
        // el mismo key
        // vuelve a pasar a futuro. Devolvemos el mismo estatus y mismo body "idéntico"
        // al original.
        try {
            String responseJson = objectMapper.writeValueAsString(transaccion);
            idempotenciaService.completar(idempotenciaKey, 201, responseJson);
        } catch (Exception e) {
            idempotenciaService.completar(idempotenciaKey, 201, null);
        }

        var uri = uriBuilder.path("/transacciones/{id}").buildAndExpand(transaccion.idTransaccion()).toUri();
        return ResponseEntity.created(uri).body(transaccion);
    }

    @GetMapping
    public ResponseEntity listarHistorial(@PageableDefault(size = 10) Pageable pageable) {
        var usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        var historial = transaccionService.consultarHistorial(usuario.getId(), pageable);

        return ResponseEntity.ok(historial);
    }

    @PostMapping("/bono")
    public ResponseEntity reclamarBono() {
        var usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var transaccion = transaccionService.otorgarBonoBienvenida(usuario.getId());
        return ResponseEntity.ok(transaccion);
    }
}
