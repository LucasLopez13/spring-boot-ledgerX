package com.lucaslopez.LedgerX.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI openAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("LedgerX API")
                                                .description("""
                                                                API REST de gestión financiera. Permite registrar usuarios, \
                                                                operar billeteras y realizar transacciones (depósito, retiro, transferencia por CBU).

                                                                **Roles:**
                                                                - `USER` — puede consultar saldo, hacer retiros y transferencias
                                                                - `ADMIN` — puede hacer depósitos además de lo anterior

                                                                **Cómo autenticarse:**
                                                                1. Registrarse con `POST /auth/registrar` o usar una cuenta existente
                                                                2. Obtener el token con `POST /auth/login`
                                                                3. Hacer click en **Authorize 🔒** y pegar el token con el prefijo `Bearer`
                                                                """)
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("Lucas Lopez")
                                                                .url("https://github.com/LucasLopez13")))
                                .components(new Components()
                                                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .in(SecurityScheme.In.HEADER)
                                                                .name("Authorization")));
        }
}
