package com.lucaslopez.LedgerX.controllers;

import com.lucaslopez.LedgerX.domain.usuarios.DatosAutenticacionUsuario;
import com.lucaslopez.LedgerX.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class AuthenticationController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity iniciarSesion(@RequestBody @Valid DatosAutenticacionUsuario datos) {

        var token = usuarioService.iniciarSesion(datos);

        return ResponseEntity.ok(token);
    }
}
