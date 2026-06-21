package com.rednorte.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Cuando un circuit breaker se abre, Spring Cloud Gateway reenvia la
 * peticion ORIGINAL (con su metodo HTTP original: GET, POST, PUT, etc.)
 * hacia la fallbackUri via forward:. Si aqui solo se mapea @GetMapping,
 * cualquier endpoint que reciba un POST/PUT/DELETE y dispare el
 * fallback termina en 405 Method Not Allowed en vez del mensaje de
 * error esperado (bug real detectado: ocurria con POST /chatbot/mensaje).
 * Por eso cada metodo usa @RequestMapping sin 'method', que acepta
 * cualquier verbo HTTP.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/usuarios")
    public ResponseEntity<Map<String, String>> usuariosFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Servicio de usuarios no disponible",
                "mensaje", "El servicio de usuarios esta temporalmente fuera de servicio. Intente nuevamente en unos momentos.",
                "servicio", "ms-usuarios"
            ));
    }

    @RequestMapping("/consultas")
    public ResponseEntity<Map<String, String>> consultasFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Servicio de consultas no disponible",
                "mensaje", "El servicio de consultas esta temporalmente fuera de servicio. Intente nuevamente en unos momentos.",
                "servicio", "ms-consultas"
            ));
    }

    @RequestMapping("/reasignacion")
    public ResponseEntity<Map<String, String>> reasignacionFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Servicio de reasignacion no disponible",
                "mensaje", "El servicio de reasignacion esta temporalmente fuera de servicio. Intente nuevamente en unos momentos.",
                "servicio", "ms-reasignacion"
            ));
    }

    @RequestMapping("/ficha")
    public ResponseEntity<Map<String, String>> fichaFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Servicio de ficha medica no disponible",
                "mensaje", "El servicio de ficha medica esta temporalmente fuera de servicio. Intente nuevamente en unos momentos.",
                "servicio", "ms-ficha-medica"
            ));
    }

    @RequestMapping("/chatbot")
    public ResponseEntity<Map<String, String>> chatbotFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "SaludBot no disponible",
                "mensaje", "SaludBot no esta disponible en este momento (puede que Ollama no este corriendo). " +
                        "Puedes agendar tu cita directamente desde la sección Agendar Cita, o intentar de nuevo en unos minutos.",
                "servicio", "ms-chatbot"
            ));
    }
}
