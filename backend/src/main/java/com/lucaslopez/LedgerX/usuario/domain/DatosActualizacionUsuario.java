package com.lucaslopez.LedgerX.usuario.domain;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DatosActualizacionUsuario(
        String nombre,
        String apellido,
        @Email String email,
        @NotBlank String passwordActual
) {
}
