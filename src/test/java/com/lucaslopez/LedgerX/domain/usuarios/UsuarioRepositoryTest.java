package com.lucaslopez.LedgerX.domain.usuarios;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EntityManager em;

    private Usuario crearUsuario(String nombre) {
        var usuario = Usuario.builder()
                .nombre(nombre)
                .email(nombre + "@test.com")
                .password("123")
                .rol(Rol.USER)
                .activo(true)
                .build();
        em.persist(usuario);
        return usuario;
    }

    @Test
    @DisplayName("Debe encontrar un usuario por su email")
    void buscarPorEmail() {
        var usuario = crearUsuario("Pepe");
        em.persist(usuario);

        var encontrado = usuarioRepository.findByEmail(usuario.getEmail());

        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getUsername()).isEqualTo("Pepe@test.com");

    }

    @Test
    @DisplayName("Debe retornar null si no existe usuario con ese email")
    void buscarPorEmailNoExiste() {
        var encontrado = usuarioRepository.findByEmail("noexiste@test.com");

        assertThat(encontrado).isNull();
    }

    @Test
    @DisplayName("Debe devolver True si el email existe")
    void existeEmail() {
        var usuario = crearUsuario("Pepito");
        em.persist(usuario);

        var existe = usuarioRepository.existsByEmail(usuario.getEmail());

        assertThat(existe).isTrue();

    }

    @Test
    @DisplayName("Debe devolver False si el email no existe")
    void noExisteEmail() {
        var existe = usuarioRepository.existsByEmail("noexiste@test.com");

        assertThat(existe).isFalse();
    }
}