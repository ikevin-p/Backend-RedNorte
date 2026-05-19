package com.example.Backend_usuarios.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("RedNorte — ms-usuarios API")
                .description("Microservicio de gestión de usuarios y roles del sistema RedNorte")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Equipo RedNorte")
                    .email("admin@rednorte.cl")
                )
            )
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Servidor local (Docker)")
            ));
    }
}
