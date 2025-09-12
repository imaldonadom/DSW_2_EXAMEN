package com.ipss.et.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI baseOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Examen 3 - Álbum API")
                .version("v1")
                .description("API para álbumes, láminas y colecciones"))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local")
            ));
    }
}
