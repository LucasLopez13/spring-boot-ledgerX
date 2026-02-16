package com.lucaslopez.LedgerX.services;

import com.lucaslopez.LedgerX.domain.usuarios.*;
import com.lucaslopez.LedgerX.infra.exception.ValidacionException;
import com.lucaslopez.LedgerX.infra.security.TokenService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @InjectMocks
    private UsuarioService usuarioService;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private BilleteraService billeteraService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuditoriaService auditoriaService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenService tokenService;

    private Usuario crearUsuario(Long id, String nombre, String email) {
        var usuario = Usuario.builder()
                .nombre(nombre)
                .email(email)
                .password("hashed_password")
                .rol(Rol.USER)
                .activo(true)
                .build();
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    @Test
    @DisplayName("Debe registrar usuario, encriptar contraseña y crear billetera")
    void registrarUsuario() {
        DatosRegistroUsuario datos = new DatosRegistroUsuario("Pepe", "Test", "pepe@gmail.com", "123456");

        when(usuarioRepository.existsByEmail(datos.email())).thenReturn(false);
        when(passwordEncoder.encode(datos.contrasenia())).thenReturn("hashed_password");

        Usuario usuarioGuardado = crearUsuario(1L, datos.nombre(), datos.email());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        var resultado = usuarioService.registrarUsuario(datos);

        assertNotNull(resultado);
        assertEquals("pepe@gmail.com", resultado.email());
        verify(usuarioRepository).save(any(Usuario.class));
        verify(billeteraService).crearBilleteraInicial(any(Usuario.class));
        verify(auditoriaService).registrarActividad(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Debe lanzar excepcion si el email ya existe")
    void registrarUsuarioEmailDuplicado() {
        DatosRegistroUsuario datos = new DatosRegistroUsuario("Pepe", "Test", "pepe@gmail.com", "123456");
        when(usuarioRepository.existsByEmail(datos.email())).thenReturn(true);

        assertThrows(ValidacionException.class, () -> usuarioService.registrarUsuario(datos));

        verify(usuarioRepository, never()).save(any());
        verify(billeteraService, never()).crearBilleteraInicial(any());
    }

    @Test
    @DisplayName("Debe generar token con credenciales validas")
    void iniciarSesionExitoso() {
        DatosAutenticacionUsuario datos = new DatosAutenticacionUsuario("pepe@gmail.com", "123456");
        Usuario usuario = crearUsuario(1L, "Pepe", "pepe@gmail.com");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(usuario);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(tokenService.generarToken(usuario)).thenReturn("token_jwt");

        var resultado = usuarioService.iniciarSesion(datos);

        assertNotNull(resultado);
        assertEquals("token_jwt", resultado.tokenJWT());
        verify(tokenService).generarToken(usuario);
        verify(auditoriaService).registrarActividad(eq(1L), any(), any(), any());
    }

    @Test
    @DisplayName("Debe lanzar excepcion y auditar login fallido")
    void iniciarSesionFallido() {
        DatosAutenticacionUsuario datos = new DatosAutenticacionUsuario("pepe@gmail.com", "wrongpassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales invalidas"));

        assertThrows(BadCredentialsException.class, () -> usuarioService.iniciarSesion(datos));

        verify(auditoriaService).registrarActividad(isNull(), any(), any(), any());
        verify(tokenService, never()).generarToken(any());
    }

    @Test
    @DisplayName("Debe retornar datos del usuario al consultar perfil")
    void consultarPerfil() {
        Usuario usuario = crearUsuario(1L, "Pepe", "pepe@gmail.com");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        var resultado = usuarioService.consultarPerfil(1L);

        assertNotNull(resultado);
        assertEquals("pepe@gmail.com", resultado.email());
    }

    @Test
    @DisplayName("Debe lanzar excepcion si usuario no existe")
    void consultarPerfilNoExiste() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> usuarioService.consultarPerfil(999L));
    }

    @Test
    @DisplayName("Debe actualizar perfil con validaciones correctas")
    void actualizarPerfil() {
        Usuario usuario = crearUsuario(1L, "Pepe", "pepe@gmail.com");

        DatosActualizacionUsuario datos = new DatosActualizacionUsuario(
                "NuevoNombre",
                "NuevoApellido",
                "nuevo@gmail.com",
                "123456");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(datos.passwordActual(), usuario.getPassword())).thenReturn(true);
        when(usuarioRepository.existsByEmail(datos.email())).thenReturn(false);

        var resultado = usuarioService.actualizarPerfil(1L, datos);

        assertNotNull(resultado);
        verify(auditoriaService).registrarActividad(eq(1L), any(), any(), any());
    }

    @Test
    @DisplayName("Debe lanzar excepcion si contraseña actual es incorrecta")
    void actualizarPerfilContraseniaIncorrecta() {
        Usuario usuario = crearUsuario(1L, "Pepe", "pepe@gmail.com");

        DatosActualizacionUsuario datos = new DatosActualizacionUsuario(
                "NuevoNombre",
                null,
                null,
                "wrongpassword");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(datos.passwordActual(), usuario.getPassword())).thenReturn(false);

        assertThrows(ValidacionException.class, () -> usuarioService.actualizarPerfil(1L, datos));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si nuevo email ya existe")
    void actualizarPerfilEmailDuplicado() {
        Usuario usuario = crearUsuario(1L, "Pepe", "pepe@gmail.com");

        DatosActualizacionUsuario datos = new DatosActualizacionUsuario(
                null,
                null,
                "existente@gmail.com",
                "123456");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(datos.passwordActual(), usuario.getPassword())).thenReturn(true);
        when(usuarioRepository.existsByEmail(datos.email())).thenReturn(true);

        assertThrows(ValidacionException.class, () -> usuarioService.actualizarPerfil(1L, datos));
    }

    @Test
    @DisplayName("Debe desactivar cuenta logicamente")
    void eliminarUsuario() {
        Usuario usuario = crearUsuario(1L, "Pepe", "pepe@gmail.com");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.eliminarUsuario(1L);

        assertFalse(usuario.isActivo());
        verify(auditoriaService).registrarActividad(eq(1L), any(), any(), any());
    }

    @Test
    @DisplayName("Debe lanzar excepcion si usuario no existe al eliminar")
    void eliminarUsuarioNoExiste() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> usuarioService.eliminarUsuario(999L));
    }
}