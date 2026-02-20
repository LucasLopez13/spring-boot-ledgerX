package com.lucaslopez.LedgerX.controllers;

import com.lucaslopez.LedgerX.domain.transacciones.DatosRegistroTransaccion;
import com.lucaslopez.LedgerX.domain.transacciones.DatosDetalleTransaccion;
import com.lucaslopez.LedgerX.domain.usuarios.Usuario;
import com.lucaslopez.LedgerX.services.IdempotenciaService;
import com.lucaslopez.LedgerX.services.TransaccionService;
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
    // Serializa/deserializa la respuesta para almacenarla en la tabla
    private ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity realizarTransaccion(
            @RequestHeader(value = "Idempotencia-Key", required = false) String idempotenciaKey,
            @RequestBody @Valid DatosRegistroTransaccion datos,
            UriComponentsBuilder uriBuilder) {

        if (idempotenciaKey == null || idempotenciaKey.isBlank()) {
            idempotenciaKey = java.util.UUID.randomUUID().toString();
        }

        var usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // 1. Verificar idempotencia, si ya fue procesada, retornar respuesta
        // almacenada
        var registroExistente = idempotenciaService.verificarOCrear(idempotenciaKey, usuario.getId());

        if (registroExistente.isPresent()) {
            var registro = registroExistente.get();
            try {
                var respuestaOriginal = objectMapper.readValue(
                        registro.getResponseBody(), DatosDetalleTransaccion.class);
                return ResponseEntity.status(registro.getResponseStatus()).body(respuestaOriginal);
            } catch (Exception e) {
                return ResponseEntity.status(registro.getResponseStatus())
                        .body(registro.getResponseBody());
            }
        }

        // 2. Ejecutar transacción normalmente
        var transaccion = transaccionService.realizarTransaccion(datos, usuario.getId());

        // 3. Almacenar resultado y marcar como completada
        try {
            String responseJson = objectMapper.writeValueAsString(transaccion);
            idempotenciaService.completar(idempotenciaKey, 201, responseJson);
        } catch (Exception e) {
            // Si falla la serialización, igual completamos sin body
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
}
