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
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class BffController {

    @Autowired
    private BffService bffService;

    // Dashboard consolidado para el admin
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> dashboard() {
        return ResponseEntity.ok(bffService.obtenerDashboard());
    }

    // Consultas de un paciente especifico
    @GetMapping("/paciente/{usuarioId}/consultas")
    public ResponseEntity<List<Map>> consultasPaciente(@PathVariable String usuarioId) {
        return ResponseEntity.ok(bffService.obtenerConsultasPaciente(usuarioId));
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
