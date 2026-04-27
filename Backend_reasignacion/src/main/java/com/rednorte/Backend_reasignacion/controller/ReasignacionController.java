package com.rednorte.Backend_reasignacion.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rednorte.Backend_reasignacion.model.Cancelacion;
import com.rednorte.Backend_reasignacion.service.ReasignacionService;

@Controller
@RestController
@RequestMapping("/api/reasignacion")
public class ReasignacionController {

    @Autowired
    private ReasignacionService reasignacionService;

    // Endpoint para cancelar sin reasignar inmediatamente
    @PostMapping("/solo-cancelar/{id}")
    public ResponseEntity<Cancelacion> cancelarCita(@PathVariable Long id, @RequestParam String motivo) {
        Cancelacion c = reasignacionService.procesarSoloCancelacion(id, motivo);
        return ResponseEntity.ok(c);
    }

    // Endpoint que hace el proceso completo (Cancelación + Reasignación)
    @PostMapping("/cancelar-y-reasignar/{id}")
    public ResponseEntity<String> procesoCompleto(@PathVariable Long id, @RequestParam String motivo) {
        try {
            Cancelacion c = reasignacionService.procesarSoloCancelacion(id, motivo);
            reasignacionService.ejecutarReasignacion(c);
            return ResponseEntity.ok("Cita cancelada y reasignación intentada exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
