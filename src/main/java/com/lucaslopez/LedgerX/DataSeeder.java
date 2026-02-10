package com.lucaslopez.LedgerX;

import com.lucaslopez.LedgerX.services.BilleteraService;
import com.lucaslopez.LedgerX.domain.usuarios.Rol;
import com.lucaslopez.LedgerX.domain.usuarios.Usuario;
import com.lucaslopez.LedgerX.domain.usuarios.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    @Value("${admin.email}")
    private String adminEmail;
    @Value("${admin.password}")
    private String adminPassword;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private BilleteraService billeteraService;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) throws Exception {
        // Verificamos si ya existe un admin para no duplicarlo cada vez que inicias la app.
        if (!usuarioRepository.existsByEmail(adminEmail)) {
            var admin = Usuario.builder()
                    .nombre("Super")
                    .apellido("Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .rol(Rol.ADMIN)
                    .activo(true)
                    .build();

            usuarioRepository.save(admin);

            billeteraService.crearBilleteraInicial(admin);
        }
    }
}
