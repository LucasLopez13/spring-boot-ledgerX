package com.lucaslopez.LedgerX.usuario.infrastructure;

import com.lucaslopez.LedgerX.billetera.domain.Billetera;
import com.lucaslopez.LedgerX.billetera.domain.BilleteraRepository;
import com.lucaslopez.LedgerX.usuario.domain.Rol;
import com.lucaslopez.LedgerX.usuario.domain.Usuario;
import com.lucaslopez.LedgerX.usuario.domain.UsuarioRepository;
import com.lucaslopez.LedgerX.shared.security.TokenService;
import com.lucaslopez.LedgerX.auditoria.application.AuditoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private BilleteraRepository billeteraRepository;
    @MockBean
    private AuditoriaService auditoriaService;

    private String tokenAdmin;

    @BeforeEach
    void setUp() {
        // Crear admin para tests que requieren rol ADMIN
        if (usuarioRepository.findByEmail("admin-ctrl@test.com") == null) {
            var admin = Usuario.builder()
                    .nombre("Admin")
                    .apellido("Test")
                    .email("admin-ctrl@test.com")
                    .password(passwordEncoder.encode("adminPass123"))
                    .rol(Rol.ADMIN)
                    .activo(true)
                    .build();
            usuarioRepository.save(admin);
            billeteraRepository.save(new Billetera(admin, BigDecimal.ZERO));
        }

        var admin = (Usuario) usuarioRepository.findByEmail("admin-ctrl@test.com");
        tokenAdmin = tokenService.generarToken(admin);
    }

    @Test
    @DisplayName("Debe retornar 201 y datos del usuario registrado")
    void registrarUsuarioExitoso() throws Exception {
        mockMvc.perform(post("/usuarios/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "Pepe",
                            "apellido": "Test",
                            "email": "pepe-ctrl@test.com",
                            "contrasenia": "password123"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Pepe"))
                .andExpect(jsonPath("$.email").value("pepe-ctrl@test.com"));
    }

    @Test
    @DisplayName("Debe retornar 400 si faltan campos obligatorios")
    void registrarUsuarioConDatosInvalidos() throws Exception {
        mockMvc.perform(post("/usuarios/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "",
                            "email": "invalido"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Debe retornar 200 con datos del perfil")
    void consultarPerfilAutenticado() throws Exception {
        mockMvc.perform(get("/usuarios/me")
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin-ctrl@test.com"));
    }

    @Test
    @DisplayName("Debe retornar 403 si no esta autenticado")
    void consultarPerfilSinAutenticar() throws Exception {
        mockMvc.perform(get("/usuarios/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Debe retornar 200 con perfil actualizado")
    void actualizarPerfilAutenticado() throws Exception {
        // Creamos un usuario especifico para este test
        var usuario = Usuario.builder()
                .nombre("Actualizar")
                .apellido("Test")
                .email("actualizar-ctrl@test.com")
                .password(passwordEncoder.encode("miPassword123"))
                .rol(Rol.USER)
                .activo(true)
                .build();
        usuarioRepository.save(usuario);
        billeteraRepository.save(new Billetera(usuario, BigDecimal.ZERO));

        String token = tokenService.generarToken(usuario);

        mockMvc.perform(put("/usuarios/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "NuevoNombre",
                            "passwordActual": "miPassword123"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("NuevoNombre"));
    }

    @Test
    @DisplayName("Debe retornar 204 si el usuario es ADMIN")
    void eliminarUsuarioComoAdmin() throws Exception {
        // Creamos un usuario a eliminar
        var usuario = Usuario.builder()
                .nombre("Eliminar")
                .apellido("Test")
                .email("eliminar-ctrl@test.com")
                .password(passwordEncoder.encode("password"))
                .rol(Rol.USER)
                .activo(true)
                .build();
        usuarioRepository.save(usuario);

        mockMvc.perform(delete("/usuarios/" + usuario.getId())
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());
    }
}
