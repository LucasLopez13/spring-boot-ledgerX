package com.lucaslopez.LedgerX.domain.usuarios;

public record DatosDetalleUsuario(
        Long id,
        String nombre,
        String apellido,
        String email
) {
    public DatosDetalleUsuario(Usuario usuario) {
        this (
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail()
        );
    }
}
