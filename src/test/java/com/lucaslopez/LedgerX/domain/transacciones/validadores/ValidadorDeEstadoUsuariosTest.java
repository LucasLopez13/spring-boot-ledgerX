package com.lucaslopez.LedgerX.domain.transacciones.validadores;

import com.lucaslopez.LedgerX.domain.billeteras.Billetera;
import com.lucaslopez.LedgerX.domain.usuarios.Usuario;
import com.lucaslopez.LedgerX.infra.exception.ValidacionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidadorDeEstadoUsuariosTest {

    @InjectMocks
    private ValidadorDeEstadoUsuarios validador;
    @Mock
    private Usuario usuarioDestino;
    @Mock
    private Usuario usuarioOrigen;
    @Mock
    private Billetera billeteraOrigen;
    @Mock
    private Billetera billeteraDestino;

    @Test
    @DisplayName("Debe lanzar excepcion si el usuario ORIGEN se encuentra inactivo")
    void validarOrigenInactivo() {
        when(billeteraOrigen.getUsuario()).thenReturn(usuarioOrigen);
        when(usuarioOrigen.isActivo()).thenReturn(false);

        assertThrows(ValidacionException.class,() -> validador.validar(billeteraOrigen,null,null));
    }
    @Test
    @DisplayName("Debe lanzar excepcion si el usuario DESTINO se encuentra inactivo")
    void validarDestinoInactivo() {
        when(billeteraOrigen.getUsuario()).thenReturn(usuarioOrigen);
        when(usuarioOrigen.isActivo()).thenReturn(true);

        when(billeteraDestino.getUsuario()).thenReturn(usuarioDestino);
        when(usuarioDestino.isActivo()).thenReturn(false);

        assertThrows(ValidacionException.class,() -> validador.validar(billeteraOrigen,billeteraDestino,null));
    }
    @Test
    @DisplayName("No Debe lanzar excepcion si ambos estan activos")
    void validarAmbosActivos() {
        when(billeteraOrigen.getUsuario()).thenReturn(usuarioOrigen);
        when(usuarioOrigen.isActivo()).thenReturn(true);

        when(billeteraDestino.getUsuario()).thenReturn(usuarioDestino);
        when(usuarioDestino.isActivo()).thenReturn(true);

        assertDoesNotThrow(() -> validador.validar(billeteraOrigen,billeteraDestino,null));
    }
    @Test
    @DisplayName("No Debe lanzar excepcion si es DEPOSITO, origen (null) y destino activo")
    void validarOrigenNull() {
        when(billeteraDestino.getUsuario()).thenReturn(usuarioDestino);
        when(usuarioDestino.isActivo()).thenReturn(true);

        assertDoesNotThrow(() -> validador.validar(null,billeteraDestino,null));
    }





}