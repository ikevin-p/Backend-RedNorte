package com.rednorte.Backend_reasignacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class Backend_reasignacion {

    public static void main(String[] args) {
        SpringApplication.run(Backend_reasignacion.class, args);
    }

    // Bean necesario para que ReasignacionService pueda hacer llamadas HTTP
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
