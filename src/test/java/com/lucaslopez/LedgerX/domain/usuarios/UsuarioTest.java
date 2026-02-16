package com.lucaslopez.LedgerX.domain.usuarios;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    private Usuario crearUsuario(String nombre) {
        var datos = new DatosRegistroUsuario(nombre,"Test",nombre + "@gmail.com","123456");

        return new Usuario(datos,"hashPassword");
    }

    @Test
    @DisplayName("El usuario debe inicializarse activo por defecto")
    void usuarioNaceActivo() {
        assertTrue(crearUsuario("Pepe").isActivo());
    }

    @Test
    @DisplayName("desactivarCuenta debe poner activo en false")
    void desactivarCuenta() {

        var usuario = crearUsuario("Fulano");

        usuario.desactivarCuenta();

        assertFalse(usuario.isEnabled());
    }
    @Test
    @DisplayName("getAuthorities debe devolver ROLE_USER por defecto")
    void rolesPorDefecto() {

        var usuario = crearUsuario("Pepito");

        Collection<? extends GrantedAuthority> authorities = usuario.getAuthorities();

        assertFalse(authorities.isEmpty());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }







}