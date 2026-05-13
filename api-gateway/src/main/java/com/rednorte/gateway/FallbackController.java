package com.rednorte.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/usuarios")
    public ResponseEntity<Map<String, String>> usuariosFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Servicio de usuarios no disponible",
                "mensaje", "El servicio de usuarios esta temporalmente fuera de servicio. Intente nuevamente en unos momentos.",
                "servicio", "ms-usuarios"
            ));
    }

    @GetMapping("/consultas")
    public ResponseEntity<Map<String, String>> consultasFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Servicio de consultas no disponible",
                "mensaje", "El servicio de consultas esta temporalmente fuera de servicio. Intente nuevamente en unos momentos.",
                "servicio", "ms-consultas"
            ));
    }

    @GetMapping("/reasignacion")
    public ResponseEntity<Map<String, String>> reasignacionFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Servicio de reasignacion no disponible",
                "mensaje", "El servicio de reasignacion esta temporalmente fuera de servicio. Intente nuevamente en unos momentos.",
                "servicio", "ms-reasignacion"
            ));
    }

    @GetMapping("/ficha")
    public ResponseEntity<Map<String, String>> fichaFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Servicio de ficha medica no disponible",
                "mensaje", "El servicio de ficha medica esta temporalmente fuera de servicio. Intente nuevamente en unos momentos.",
                "servicio", "ms-ficha-medica"
            ));
    }
}
