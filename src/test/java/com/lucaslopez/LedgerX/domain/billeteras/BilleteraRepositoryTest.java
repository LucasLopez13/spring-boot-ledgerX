package com.lucaslopez.LedgerX.domain.billeteras;

import com.lucaslopez.LedgerX.domain.usuarios.Rol;
import com.lucaslopez.LedgerX.domain.usuarios.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BilleteraRepositoryTest {

    @Autowired
    private BilleteraRepository billeteraRepository;
    @Autowired
    private TestEntityManager em;

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
    @DisplayName("Debe encontrar billetera por ID de usuario")
    void buscarPorUsuarioID() {

        var usuario = crearUsuario("Pepe");
        var billetera = new Billetera(usuario, new BigDecimal("100"));

        em.persist(billetera);

        var encontrada = billeteraRepository.findByUsuarioId(usuario.getId());

        assertThat(encontrada).isNotNull();
        assertThat(encontrada.getSaldo()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("Debe retornar null si no existe billetera para el usuario")
    void buscarPorUsuarioIDNoExiste() {
        var resultado = billeteraRepository.findByUsuarioId(999L);

        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("Debe traer billetera con Lock")
    void buscarConLock() {

        var usuario = crearUsuario("Fulano");
        var billetera = new Billetera(usuario, new BigDecimal("500"));

        em.persist(billetera);
        em.flush(); // Forzar guardado

        Optional<Billetera> resultado = billeteraRepository.findByIdWithLock(billetera.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(billetera.getId());
    }

    @Test
    @DisplayName("Debe retornar Optional vacio si no existe billetera con ese ID")
    void buscarConLockNoExiste() {
        Optional<Billetera> resultado = billeteraRepository.findByIdWithLock(999L);

        assertThat(resultado).isEmpty();
    }
}