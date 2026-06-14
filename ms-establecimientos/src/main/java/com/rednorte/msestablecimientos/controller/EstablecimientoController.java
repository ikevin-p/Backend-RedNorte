package com.rednorte.msestablecimientos.controller;

import com.rednorte.msestablecimientos.model.Establecimiento;
import com.rednorte.msestablecimientos.service.EstablecimientoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/establecimientos")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class EstablecimientoController {

    private final EstablecimientoService service;

    public EstablecimientoController(EstablecimientoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Establecimiento>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<Establecimiento>> listarActivos() {
        return ResponseEntity.ok(service.listarActivos());
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<?> listarPorTipo(@PathVariable String tipo) {
        try {
            return ResponseEntity.ok(service.listarPorTipo(tipo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Tipo inválido. Use: HOSPITAL, CLINICA, CESFAM, CONSULTORIO, POSTA_RURAL"));
        }
    }

    @GetMapping("/comuna/{comuna}")
    public ResponseEntity<List<Establecimiento>> listarPorComuna(@PathVariable String comuna) {
        return ResponseEntity.ok(service.listarPorComuna(comuna));
    }

    @GetMapping("/region/{region}")
    public ResponseEntity<List<Establecimiento>> listarPorRegion(@PathVariable String region) {
        return ResponseEntity.ok(service.listarPorRegion(region));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.obtenerPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Establecimiento establecimiento) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(establecimiento));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
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
