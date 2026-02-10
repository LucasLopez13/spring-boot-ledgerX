package com.lucaslopez.LedgerX.controllers;

import com.lucaslopez.LedgerX.domain.transacciones.DatosRegistroTransaccion;
import com.lucaslopez.LedgerX.domain.usuarios.Usuario;
import com.lucaslopez.LedgerX.services.TransaccionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/transacciones")
public class TransaccionController {
    @Autowired
    private TransaccionService transaccionService;

    @PostMapping
    public ResponseEntity realizarTransaccion(@RequestBody @Valid DatosRegistroTransaccion datos, UriComponentsBuilder uriBuilder) {
        var usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        var transaccion = transaccionService.realizarTransaccion(datos,usuario.getId());

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
