package com.lucaslopez.LedgerX.controllers;

import com.lucaslopez.LedgerX.domain.billeteras.Billetera;
import com.lucaslopez.LedgerX.domain.billeteras.BilleteraRepository;
import com.lucaslopez.LedgerX.domain.usuarios.Rol;
import com.lucaslopez.LedgerX.domain.usuarios.Usuario;
import com.lucaslopez.LedgerX.domain.usuarios.UsuarioRepository;
import com.lucaslopez.LedgerX.infra.security.TokenService;
import com.lucaslopez.LedgerX.services.AuditoriaService;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransaccionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private BilleteraRepository billeteraRepository;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockBean
    private AuditoriaService auditoriaService;

    private String tokenAdmin;
    private String tokenUsuario;

    @BeforeEach
    void setUp() {
        if (usuarioRepository.findByEmail("trans-admin@test.com") == null) {
            var admin = Usuario.builder()
                    .nombre("TransAdmin")
                    .apellido("Test")
                    .email("trans-admin@test.com")
                    .password(passwordEncoder.encode("password123"))
                    .rol(Rol.ADMIN)
                    .activo(true)
                    .build();
            usuarioRepository.save(admin);
            billeteraRepository.save(new Billetera(admin, new BigDecimal("1000.00")));
        }

        if (usuarioRepository.findByEmail("trans-user@test.com") == null) {
            var usuario = Usuario.builder()
                    .nombre("TransUser")
                    .apellido("Test")
                    .email("trans-user@test.com")
                    .password(passwordEncoder.encode("password123"))
                    .rol(Rol.USER)
                    .activo(true)
                    .build();
            usuarioRepository.save(usuario);
            billeteraRepository.save(new Billetera(usuario, new BigDecimal("1000.00")));
        }

        var admin = (Usuario) usuarioRepository.findByEmail("trans-admin@test.com");
        tokenAdmin = tokenService.generarToken(admin);

        var usuario = (Usuario) usuarioRepository.findByEmail("trans-user@test.com");
        tokenUsuario = tokenService.generarToken(usuario);
    }

    @Test
    @DisplayName("Debe retornar 201 al realizar un deposito (ADMIN)")
    void realizarDepositoExitoso() throws Exception {
        mockMvc.perform(post("/transacciones")
                .header("Authorization", "Bearer " + tokenAdmin)
                .header("Idempotencia-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "monto": 500.00,
                            "descripcion": "Deposito de prueba",
                            "tipoTransaccion": "DEPOSITO"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoTransaccion").value("DEPOSITO"))
                .andExpect(jsonPath("$.estadoTransaccion").value("EXITOSO"));
    }

    @Test
    @DisplayName("Debe retornar 403 si no esta autenticado")
    void realizarTransaccionSinAutenticar() throws Exception {
        mockMvc.perform(post("/transacciones")
                .header("Idempotencia-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "monto": 100.00,
                            "descripcion": "Sin auth",
                            "tipoTransaccion": "DEPOSITO"
                        }
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Debe retornar 400 si faltan campos obligatorios")
    void realizarTransaccionConDatosInvalidos() throws Exception {
        mockMvc.perform(post("/transacciones")
                .header("Authorization", "Bearer " + tokenUsuario)
                .header("Idempotencia-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "descripcion": ""
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Debe retornar la misma respuesta si se usa la misma Idempotencia-Key")
    void idempotenciaConMismaKey() throws Exception {
        String idempotenciaKey = UUID.randomUUID().toString();

        // Debe crear la transacción (201)
        var primeraRespuesta = mockMvc.perform(post("/transacciones")
                .header("Authorization", "Bearer " + tokenAdmin)
                .header("Idempotencia-Key", idempotenciaKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "monto": 200.00,
                            "descripcion": "Deposito idempotente",
                            "tipoTransaccion": "DEPOSITO"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoTransaccion").value("DEPOSITO"))
                .andReturn();

        // Segunda request con misma key, Debe retornar la respuesta original
        mockMvc.perform(post("/transacciones")
                .header("Authorization", "Bearer " + tokenAdmin)
                .header("Idempotencia-Key", idempotenciaKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "monto": 200.00,
                            "descripcion": "Deposito idempotente",
                            "tipoTransaccion": "DEPOSITO"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoTransaccion").value("DEPOSITO"))
                .andExpect(jsonPath("$.estadoTransaccion").value("EXITOSO"));
    }
}
