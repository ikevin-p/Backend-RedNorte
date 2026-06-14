package com.rednorte.msestadisticas.controller;

import com.rednorte.msestadisticas.dto.EstadisticasDTO;
import com.rednorte.msestadisticas.service.EstadisticasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/estadisticas")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@Tag(name = "Estadísticas", description = "Reportes y métricas del sistema RedNorte")
public class EstadisticasController {

    private final EstadisticasService service;

    public EstadisticasController(EstadisticasService service) {
        this.service = service;
    }

    @GetMapping("/resumen")
    @Operation(summary = "Resumen general del sistema — totales y distribuciones de todos los módulos")
    public ResponseEntity<EstadisticasDTO> resumen() {
        return ResponseEntity.ok(service.obtenerResumen());
    }

    @GetMapping("/consultas")
    @Operation(summary = "Estadísticas de consultas médicas — por estado y por doctor")
    public ResponseEntity<Map<String, Object>> consultas() {
        return ResponseEntity.ok(service.estadisticasConsultas());
    }

    @GetMapping("/agenda")
    @Operation(summary = "Estadísticas de agenda médica — bloques por estado y por doctor")
    public ResponseEntity<Map<String, Object>> agenda() {
        return ResponseEntity.ok(service.estadisticasAgenda());
    }
}
