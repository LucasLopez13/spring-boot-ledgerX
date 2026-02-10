package com.lucaslopez.LedgerX.services;

import com.lucaslopez.LedgerX.domain.logActivitys.TipoAccion;
import com.lucaslopez.LedgerX.domain.usuarios.*;
import com.lucaslopez.LedgerX.infra.exception.ValidacionException;
import com.lucaslopez.LedgerX.infra.security.DatosTokenJWT;
import com.lucaslopez.LedgerX.infra.security.TokenService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private BilleteraService billeteraService;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private AuditoriaService auditoriaService;
    @Autowired
    @Lazy // Usamos @Lazy para evitar cualquier riesgo de ciclo con Security
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenService tokenService;



    @Transactional
    public DatosDetalleUsuario registrarUsuario(DatosRegistroUsuario datos) {
        validarEmailUnico(datos.email());

        var passwordHash = encoder.encode(datos.contrasenia());

        var usuario = new Usuario(datos, passwordHash);

        usuarioRepository.save(usuario);

        billeteraService.crearBilleteraInicial(usuario);

        auditoriaService.registrarActividad(
                usuario.getId(),
                TipoAccion.REGISTRO_USUARIO,
                "Usuario registrado con email: " + usuario.getEmail(),
                "127.0.0.1"
        );

        //Devuelvo un DTO para no devolver el usuario con la contraseña.
        return new DatosDetalleUsuario(usuario);

    }

    public DatosTokenJWT iniciarSesion(DatosAutenticacionUsuario datos) {
        try {
            var authenticationToken = new UsernamePasswordAuthenticationToken(datos.email(), datos.contrasenia());
            var authentication = authenticationManager.authenticate(authenticationToken);

            var usuario = (Usuario) authentication.getPrincipal();

            var tokenJWT = tokenService.generarToken((Usuario) authentication.getPrincipal());

            auditoriaService.registrarActividad(
                    usuario.getId(),
                    TipoAccion.LOGIN_EXITOSO,
                    "Inicio de sesion via API",
                    "127.0.0.1"
            );

            return new DatosTokenJWT(tokenJWT);
        }
        catch (AuthenticationException e) {
            auditoriaService.registrarActividad(
                    null,
                    TipoAccion.LOGIN_FALLIDO,
                    "Fallo al intentar entrar con el email: " + datos.email(),
                    "127.0.0.1"
            );

            throw e;
        }
    }

    public DatosDetalleUsuario consultarPerfil(Long idUsuario) {
        var usuario = buscarUsuarioPorId(idUsuario);

        return new DatosDetalleUsuario(usuario);
    }

    @Transactional
    public DatosDetalleUsuario actualizarPerfil(Long id, DatosActualizacionUsuario datos) {
        var usuario = buscarUsuarioPorId(id);

        validarContrasenia(datos,usuario);

        validarEmailEnActualizacion(datos,usuario);

        usuario.actualizarUsuario(datos);

        auditoriaService.registrarActividad(
                id,
                TipoAccion.CAMBIO_PERFIL_REALIZADO,
                "Perfil actualizado con exito",
                "127.0.0.1"
        );

        return new DatosDetalleUsuario(usuario);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        var usuario = buscarUsuarioPorId(id);

        usuario.desactivarCuenta();

        auditoriaService.registrarActividad(
                id,
                TipoAccion.CUENTA_SUSPENDIDA,
                "Cuenta suspendida/eliminada logicamente",
                "127.0.0.1"
        );
    }

    private void validarEmailUnico(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new ValidacionException("El email ingresado ya se encuentra registrado.");
        }
    }

    private void validarEmailEnActualizacion(DatosActualizacionUsuario datos, Usuario usuario) {
        if (datos.email() != null && !datos.email().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(datos.email())) {
                throw new ValidacionException("El email ya se encuentra en el sistema");
            }
        }
    }

    private void validarContrasenia(DatosActualizacionUsuario datos, Usuario usuario) {
        if (!encoder.matches(datos.passwordActual(),usuario.getPassword())) {
            throw new ValidacionException("La contraseña ingresada es incorrecta. No se puede aplicar cambios");
        }
    }

    private Usuario buscarUsuarioPorId(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("No se encontro un usuario con ese id"));
    }
}
