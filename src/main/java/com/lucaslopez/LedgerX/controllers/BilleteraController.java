package com.lucaslopez.LedgerX.controllers;

import com.lucaslopez.LedgerX.domain.usuarios.Usuario;
import com.lucaslopez.LedgerX.services.BilleteraService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/billeteras")
@SecurityRequirement(name = "bearer-jwt")
public class BilleteraController {

    @Autowired
    private BilleteraService billeteraService;

    @GetMapping("/saldo")
    public ResponseEntity consultarSaldo() {

        var usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        var datosSaldo = billeteraService.consultarSaldo(usuario.getId());

        return ResponseEntity.ok().body(datosSaldo);
    }
}
