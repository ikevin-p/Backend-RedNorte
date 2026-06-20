package com.rednorte.msestablecimientos.controller;

import com.rednorte.msestablecimientos.model.Establecimiento;
import com.rednorte.msestablecimientos.service.EstablecimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/establecimientos")
// NOTA: el CORS lo maneja exclusivamente el API Gateway (globalcors).
@Tag(name = "Establecimientos", description = "Catálogo de hospitales, clínicas, CESFAM, consultorios y postas rurales")
public class EstablecimientoController {

    private final EstablecimientoService service;

    public EstablecimientoController(EstablecimientoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todos los establecimientos")
    public ResponseEntity<List<Establecimiento>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar establecimientos activos")
    public ResponseEntity<List<Establecimiento>> listarActivos() {
        return ResponseEntity.ok(service.listarActivos());
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Listar por tipo", description = "Valores válidos: HOSPITAL, CLINICA, CESFAM, CONSULTORIO, POSTA_RURAL")
    public ResponseEntity<?> listarPorTipo(@PathVariable String tipo) {
        try {
            return ResponseEntity.ok(service.listarPorTipo(tipo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Tipo inválido. Use: HOSPITAL, CLINICA, CESFAM, CONSULTORIO, POSTA_RURAL"));
        }
    }

    @GetMapping("/comuna/{comuna}")
    @Operation(summary = "Listar por comuna")
    public ResponseEntity<List<Establecimiento>> listarPorComuna(@PathVariable String comuna) {
        return ResponseEntity.ok(service.listarPorComuna(comuna));
    }

    @GetMapping("/region/{region}")
    @Operation(summary = "Listar por región")
    public ResponseEntity<List<Establecimiento>> listarPorRegion(@PathVariable String region) {
        return ResponseEntity.ok(service.listarPorRegion(region));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener establecimiento por ID")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.obtenerPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    @Operation(summary = "Crear establecimiento", description = "Solo administrador")
    public ResponseEntity<?> crear(@RequestBody Establecimiento establecimiento) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(establecimiento));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar establecimiento", description = "Solo administrador")
    public ResponseEntity<?> actualizar(@PathVariable String id,
                                        @RequestBody Establecimiento datos) {
        try {
            return ResponseEntity.ok(service.actualizar(id, datos));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar establecimiento", description = "Solo administrador")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        try {
            service.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
