package com.rednorte.msestadisticas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MsEstadisticasApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsEstadisticasApplication.class, args);
    }
}
