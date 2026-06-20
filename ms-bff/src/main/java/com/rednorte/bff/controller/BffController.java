package com.rednorte.bff.controller;

import com.rednorte.bff.dto.DashboardDTO;
import com.rednorte.bff.service.BffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "BFF", description = "Backend For Frontend: agrega datos de varios microservicios para el dashboard del administrador")
public class BffController {

    @Autowired
    private BffService bffService;

    // Dashboard consolidado para el admin
    // El token JWT del usuario se reenvia a los microservicios internos
    // (ms-consultas, ms-usuarios), ya que ahora exigen autenticacion.
    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard consolidado", description = "Agrega totales de consultas y usuarios desde ms-consultas y ms-usuarios en una sola respuesta")
    public ResponseEntity<DashboardDTO> dashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(bffService.obtenerDashboard(authHeader));
    }

    // Consultas de un paciente especifico
    @GetMapping("/paciente/{usuarioId}/consultas")
    @Operation(summary = "Consultas de un paciente", description = "Reenvía la consulta a ms-consultas propagando el token JWT")
    public ResponseEntity<List<Map>> consultasPaciente(
            @PathVariable String usuarioId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(bffService.obtenerConsultasPaciente(usuarioId, authHeader));
    }

    // Health check del BFF
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Endpoint público, no requiere autenticación")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "servicio", "ms-bff",
            "descripcion", "Backend For Frontend - RedNorte"
        ));
    }
}
