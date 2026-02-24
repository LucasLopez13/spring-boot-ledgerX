package com.lucaslopez.LedgerX.usuario.domain;

import jakarta.validation.constraints.NotBlank;

public record DatosAutenticacionUsuario(
        @NotBlank String email,
        @NotBlank String contrasenia
) {
}
