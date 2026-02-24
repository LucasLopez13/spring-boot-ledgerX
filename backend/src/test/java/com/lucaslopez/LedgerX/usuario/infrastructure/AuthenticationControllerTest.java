package com.lucaslopez.LedgerX.usuario.infrastructure;

import com.lucaslopez.LedgerX.usuario.domain.Rol;
import com.lucaslopez.LedgerX.usuario.domain.Usuario;
import com.lucaslopez.LedgerX.usuario.domain.UsuarioRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockBean
    private AuditoriaService auditoriaService;

    @BeforeEach
    void setUp() {
        if (usuarioRepository.findByEmail("login@test.com") == null) {
            var usuario = Usuario.builder()
                    .nombre("Login")
                    .apellido("Test")
                    .email("login@test.com")
                    .password(passwordEncoder.encode("password123"))
                    .rol(Rol.USER)
                    .activo(true)
                    .build();
            usuarioRepository.save(usuario);
        }
    }

    @Test
    @DisplayName("Debe retornar 200 y token JWT con credenciales validas")
    void iniciarSesionExitoso() throws Exception {
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "email": "login@test.com",
                            "contrasenia": "password123"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenJWT").isNotEmpty());
    }

    @Test
    @DisplayName("Debe retornar 401 con credenciales invalidas")
    void iniciarSesionFallido() throws Exception {
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "email": "login@test.com",
                            "contrasenia": "passwordIncorrecta"
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }
}
