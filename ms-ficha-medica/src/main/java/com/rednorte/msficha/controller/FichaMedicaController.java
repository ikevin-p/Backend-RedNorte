package com.rednorte.msficha.controller;

import com.rednorte.msficha.model.FichaMedica;
import com.rednorte.msficha.service.FichaMedicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ficha")
// NOTA: el CORS lo maneja exclusivamente el API Gateway (globalcors).
@Tag(name = "Ficha Médica", description = "Ficha clínica digital del paciente: signos vitales, alergias, antecedentes")
public class FichaMedicaController {

    @Autowired
    private FichaMedicaService service;

    @GetMapping("/{usuarioId}")
    @Operation(summary = "Obtener ficha médica", description = "Retorna la ficha clínica de un usuario, o 204 si aún no la ha completado")
    public ResponseEntity<FichaMedica> obtener(@PathVariable String usuarioId) {
        FichaMedica ficha = service.obtenerPorUsuario(usuarioId);
        if (ficha == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(ficha);
    }

    @PutMapping("/{usuarioId}")
    @Operation(summary = "Crear o actualizar ficha médica", description = "Si el usuario no tiene ficha previa, la crea; si ya existe, la actualiza")
    public ResponseEntity<FichaMedica> guardar(@PathVariable String usuarioId,
                                                @RequestBody FichaMedica datos) {
        return ResponseEntity.ok(service.guardar(usuarioId, datos));
    }
}
