package com.lucaslopez.LedgerX.usuario.infrastructure;

import com.lucaslopez.LedgerX.usuario.domain.DatosActualizacionUsuario;
import com.lucaslopez.LedgerX.usuario.domain.DatosRegistroUsuario;
import com.lucaslopez.LedgerX.usuario.domain.Usuario;
import com.lucaslopez.LedgerX.usuario.application.UsuarioService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    public ResponseEntity registrarUsuario(@RequestBody @Valid DatosRegistroUsuario datos,
            UriComponentsBuilder uriBuilder) {
        var usuario = usuarioService.registrarUsuario(datos);

        var uri = uriBuilder.path("/usuarios/{id}").buildAndExpand(usuario.id()).toUri();
        return ResponseEntity.created(uri).body(usuario);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity obtenerMiPerfil() {
        var usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        var perfil = usuarioService.consultarPerfil(usuario.getId());

        return ResponseEntity.ok(perfil);
    }

    @PutMapping("/me")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity actualizarMiPerfil(@RequestBody @Valid DatosActualizacionUsuario datos) {
        var usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        var perfilActualizado = usuarioService.actualizarPerfil(usuario.getId(), datos);

        return ResponseEntity.ok(perfilActualizado);
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearer-jwt")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);

        return ResponseEntity.noContent().build();
    }
}
