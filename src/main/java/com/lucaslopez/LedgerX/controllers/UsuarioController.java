package com.lucaslopez.LedgerX.controllers;

import com.lucaslopez.LedgerX.domain.usuarios.DatosActualizacionUsuario;
import com.lucaslopez.LedgerX.domain.usuarios.DatosRegistroUsuario;
import com.lucaslopez.LedgerX.domain.usuarios.Usuario;
import com.lucaslopez.LedgerX.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/registrar")
    public ResponseEntity registrarUsuario(@RequestBody @Valid DatosRegistroUsuario datos, UriComponentsBuilder uriBuilder){
        var usuario = usuarioService.registrarUsuario(datos);

        var uri = uriBuilder.path("/usuarios/{id}").buildAndExpand(usuario.id()).toUri();
        return ResponseEntity.created(uri).body(usuario);
    }

    @GetMapping("/me")
    public ResponseEntity obtenerMiPerfil() {
        var usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        var perfil = usuarioService.consultarPerfil(usuario.getId());

        return ResponseEntity.ok(perfil);
    }

    @PutMapping("/me")
    public ResponseEntity actualizarMiPerfil(@RequestBody @Valid DatosActualizacionUsuario datos) {
        var usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        var perfilActualizado = usuarioService.actualizarPerfil(usuario.getId(),datos);

        return ResponseEntity.ok(perfilActualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);

        return ResponseEntity.noContent().build();
    }
}
