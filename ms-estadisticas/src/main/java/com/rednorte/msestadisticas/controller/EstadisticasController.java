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
// NOTA: el CORS lo maneja exclusivamente el API Gateway (globalcors).
// @CrossOrigin aqui duplicaria el header Access-Control-Allow-Origin
// y el navegador bloquearia la respuesta.
@Tag(name = "Estadísticas", description = "Reportes y métricas del sistema RedNorte")
public class EstadisticasController {

    private final EstadisticasService service;

    public EstadisticasController(EstadisticasService service) {
        this.service = service;
    }

    @GetMapping("/resumen")
    @Operation(summary = "Resumen general del sistema — totales y distribuciones de todos los módulos")
    public ResponseEntity<EstadisticasDTO> resumen(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(service.obtenerResumen(authHeader));
    }

    @GetMapping("/consultas")
    @Operation(summary = "Estadísticas de consultas médicas — por estado y por doctor")
    public ResponseEntity<Map<String, Object>> consultas(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(service.estadisticasConsultas(authHeader));
    }

    @GetMapping("/agenda")
    @Operation(summary = "Estadísticas de agenda médica — bloques por estado y por doctor")
    public ResponseEntity<Map<String, Object>> agenda(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(service.estadisticasAgenda(authHeader));
    }
}
