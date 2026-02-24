package com.lucaslopez.LedgerX.billetera.infrastructure;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BilleteraControllerTest {

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

    private String tokenUsuario;

    @BeforeEach
    void setUp() {
        if (usuarioRepository.findByEmail("billetera@test.com") == null) {
            var usuario = Usuario.builder()
                    .nombre("Billetera")
                    .apellido("Test")
                    .email("billetera@test.com")
                    .password(passwordEncoder.encode("password123"))
                    .rol(Rol.USER)
                    .activo(true)
                    .build();
            usuarioRepository.save(usuario);
            billeteraRepository.save(new Billetera(usuario, new BigDecimal("500.00")));
        }

        var usuario = (Usuario) usuarioRepository.findByEmail("billetera@test.com");
        tokenUsuario = tokenService.generarToken(usuario);
    }

    @Test
    @DisplayName("Debe retornar 200 con el saldo del usuario")
    void consultarSaldoAutenticado() throws Exception {
        mockMvc.perform(get("/billeteras/saldo")
                .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailUsuario").value("billetera@test.com"))
                .andExpect(jsonPath("$.saldo").isNumber());
    }

    @Test
    @DisplayName("Debe retornar 403 si no esta autenticado")
    void consultarSaldoSinAutenticar() throws Exception {
        mockMvc.perform(get("/billeteras/saldo"))
                .andExpect(status().isForbidden());
    }
}
