package com.lucaslopez.LedgerX.domain.transacciones.validadores;

import com.lucaslopez.LedgerX.domain.billeteras.Billetera;
import com.lucaslopez.LedgerX.infra.exception.ValidacionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidadorDeUsuarioTest {

    @InjectMocks
    private ValidadorDeUsuario validadorDeUsuario;
    @Mock
    private Billetera billeteraDestino;
    @Mock
    private Billetera billeteraOrigen;

    @Test
    @DisplayName("Debe lanzar excepcion si usuario y destino son la misma billeteras")
    void validarMismoUsuario() {
        when(billeteraOrigen.getId()).thenReturn(1L);
        when(billeteraDestino.getId()).thenReturn(1L);

        assertThrows(ValidacionException.class,() -> validadorDeUsuario.validar(billeteraOrigen,billeteraDestino,null));
    }

    @Test
    @DisplayName("No debe lanzar excpecion si usuario y destino son billeteras distintas")
    void validarUsuariosDiferentes() {
        when(billeteraOrigen.getId()).thenReturn(1L);
        when(billeteraDestino.getId()).thenReturn(2L);

        assertDoesNotThrow(() -> validadorDeUsuario.validar(billeteraOrigen,billeteraDestino,null));
    }

    @Test
    @DisplayName("No debe validar si alguno es null (Desposito o retiro)")
    void validarConNull() {
        assertDoesNotThrow(() -> validadorDeUsuario.validar(mock(Billetera.class),null,null));
        assertDoesNotThrow(() -> validadorDeUsuario.validar(null,mock(Billetera.class),null));
    }
}