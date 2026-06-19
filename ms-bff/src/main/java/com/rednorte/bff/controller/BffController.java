package com.rednorte.bff.controller;

import com.rednorte.bff.dto.DashboardDTO;
import com.rednorte.bff.service.BffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bff")
// NOTA: el CORS lo maneja exclusivamente el API Gateway (globalcors).
// @CrossOrigin aqui duplicaria el header Access-Control-Allow-Origin
// y el navegador bloquearia la respuesta.
public class BffController {

    @Autowired
    private BffService bffService;

    // Dashboard consolidado para el admin
    // El token JWT del usuario se reenvia a los microservicios internos
    // (ms-consultas, ms-usuarios), ya que ahora exigen autenticacion.
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> dashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(bffService.obtenerDashboard(authHeader));
    }

    // Consultas de un paciente especifico
    @GetMapping("/paciente/{usuarioId}/consultas")
    public ResponseEntity<List<Map>> consultasPaciente(
            @PathVariable String usuarioId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(bffService.obtenerConsultasPaciente(usuarioId, authHeader));
    }

    // Health check del BFF
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "servicio", "ms-bff",
            "descripcion", "Backend For Frontend - RedNorte"
        ));
    }
}
