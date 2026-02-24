package com.lucaslopez.LedgerX.shared.security;

import com.lucaslopez.LedgerX.usuario.domain.Rol;
import com.lucaslopez.LedgerX.usuario.domain.Usuario;
import com.lucaslopez.LedgerX.usuario.domain.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @InjectMocks
    private SecurityFilter securityFilter;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private TokenService tokenService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private Usuario crearUsuario(Long id, String email) {
        var usuario = Usuario.builder()
                .nombre("Test")
                .email(email)
                .password("hashed_password")
                .rol(Rol.USER)
                .activo(true)
                .build();
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    @Test
    @DisplayName("Debe autenticar usuario si el token es valido")
    void filtroConTokenValido() throws Exception {
        SecurityContextHolder.clearContext();

        Usuario usuario = crearUsuario(1L, "pepe@gmail.com");

        when(request.getHeader("Authorization")).thenReturn("Bearer token_valido");
        when(tokenService.getSubject("token_valido")).thenReturn("pepe@gmail.com");
        when(usuarioRepository.findByEmail("pepe@gmail.com")).thenReturn(usuario);

        securityFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(usuario, SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe continuar sin autenticar si no hay token")
    void filtroSinToken() throws Exception {
        SecurityContextHolder.clearContext();

        when(request.getHeader("Authorization")).thenReturn(null);

        securityFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Debe continuar sin autenticar si el usuario no existe")
    void filtroConTokenPeroUsuarioNoExiste() throws Exception {
        SecurityContextHolder.clearContext();

        when(request.getHeader("Authorization")).thenReturn("Bearer token_valido");
        when(tokenService.getSubject("token_valido")).thenReturn("noexiste@gmail.com");
        when(usuarioRepository.findByEmail("noexiste@gmail.com")).thenReturn(null);

        securityFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Debe extraer token del header Authorization")
    void recuperarTokenConBearer() {
        when(request.getHeader("Authorization")).thenReturn("Bearer mi_token_jwt");

        String token = securityFilter.recuperarToken(request);

        assertEquals("mi_token_jwt", token);
    }

    @Test
    @DisplayName("Debe retornar null si no hay header Authorization")
    void recuperarTokenSinHeader() {
        when(request.getHeader("Authorization")).thenReturn(null);

        String token = securityFilter.recuperarToken(request);

        assertNull(token);
    }
}
