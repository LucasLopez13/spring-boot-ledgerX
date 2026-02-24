package com.lucaslopez.LedgerX.shared.security;

import com.lucaslopez.LedgerX.usuario.domain.Rol;
import com.lucaslopez.LedgerX.usuario.domain.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;

    private Usuario crearUsuario(Long id, String email, Rol rol) {
        var usuario = Usuario.builder()
                .nombre("Test")
                .email(email)
                .password("hashed_password")
                .rol(rol)
                .activo(true)
                .build();
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", "test-secret-key-12345");
    }

    @Test
    @DisplayName("Debe generar un token JWT valido")
    void generarTokenExitoso() {
        Usuario usuario = crearUsuario(1L, "pepe@gmail.com", Rol.USER);

        String token = tokenService.generarToken(usuario);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Debe contener el email del usuario como subject")
    void generarTokenContieneSubjectCorrecto() {
        Usuario usuario = crearUsuario(1L, "pepe@gmail.com", Rol.USER);

        String token = tokenService.generarToken(usuario);
        DecodedJWT decoded = JWT.decode(token);

        assertEquals("pepe@gmail.com", decoded.getSubject());
    }

    @Test
    @DisplayName("Debe contener los claims de id y rol")
    void generarTokenContieneClaims() {
        Usuario usuario = crearUsuario(5L, "admin@gmail.com", Rol.ADMIN);

        String token = tokenService.generarToken(usuario);
        DecodedJWT decoded = JWT.decode(token);

        assertEquals(5L, decoded.getClaim("id").asLong());
        assertEquals("ADMIN", decoded.getClaim("rol").asString());
    }

    @Test
    @DisplayName("Debe retornar el subject de un token valido")
    void getSubjectExitoso() {
        Usuario usuario = crearUsuario(1L, "pepe@gmail.com", Rol.USER);
        String token = tokenService.generarToken(usuario);

        String subject = tokenService.getSubject(token);

        assertEquals("pepe@gmail.com", subject);
    }

    @Test
    @DisplayName("Debe lanzar excepcion si el token es invalido")
    void getSubjectTokenInvalido() {
        assertThrows(RuntimeException.class, () -> tokenService.getSubject("token.invalido.aqui"));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si el token esta expirado")
    void getSubjectTokenExpirado() {
        Usuario usuario = crearUsuario(1L, "pepe@gmail.com", Rol.USER);
        String token = tokenService.generarToken(usuario);

        // Cambiar el secret para simular un token firmado con otro secreto (invalido)
        ReflectionTestUtils.setField(tokenService, "secret", "otro-secret-diferente");

        assertThrows(RuntimeException.class, () -> tokenService.getSubject(token));
    }

    @Test
    @DisplayName("Debe retornar una fecha de expiracion futura")
    void fechaDeExpiracionFutura() {
        Instant expiracion = tokenService.fechaDeExpiracion();

        assertTrue(expiracion.isAfter(Instant.now()));
    }
}
