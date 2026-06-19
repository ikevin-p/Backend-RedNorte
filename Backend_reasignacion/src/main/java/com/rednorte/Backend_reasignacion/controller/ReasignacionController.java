package com.rednorte.Backend_reasignacion.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.rednorte.Backend_reasignacion.model.Cancelacion;
import com.rednorte.Backend_reasignacion.service.ReasignacionService;

@RestController
@RequestMapping("/api/reasignacion")
// NOTA: el CORS lo maneja exclusivamente el API Gateway (globalcors).
public class ReasignacionController {
    @Autowired
    private ReasignacionService reasignacionService;

    @PostMapping("/solo-cancelar/{id}")
    public ResponseEntity<Cancelacion> cancelarCita(@PathVariable Long id, @RequestParam String motivo) {
        Cancelacion c = reasignacionService.procesarSoloCancelacion(id, motivo);
        return ResponseEntity.ok(c);
    }

    // El token JWT del admin se reenvia a ms-consultas, que ahora exige
    // autenticacion en todos sus endpoints (incluido /consultas/prioritario/**).
    @PostMapping("/cancelar-y-reasignar/{id}")
    public ResponseEntity<String> procesoCompleto(
            @PathVariable Long id,
            @RequestParam String motivo,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Cancelacion c = reasignacionService.procesarSoloCancelacion(id, motivo);
            reasignacionService.ejecutarReasignacion(c, authHeader);
            return ResponseEntity.ok("Cita cancelada y reasignación intentada exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
