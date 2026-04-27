package com.rednorte.Backend_reasignacion.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rednorte.Backend_reasignacion.service.ReasignacionService;

@RestController
@RequestMapping("/api/reasignacion")
public class ReasignacionController {

    @Autowired
    private ReasignacionService reasignacionService;

    /**
     *
     * @param id Corresponde al BloquesAgendaid de tu modelo.
     * @param motivo Razón de la cancelación enviada desde el portal.
     */
    @PostMapping("/cancelar/{id}")
    public ResponseEntity<String> procesarCancelacion(
            @PathVariable("id") Long id,
            @RequestParam String motivo) {
        try {
            // Llamamos al service que usa RestTemplate para hablar con el otro backend en Docker
            reasignacionService.ejecutarReasignacionAutomatica(id, motivo);

            return ResponseEntity.ok("Proceso de cancelación y reasignación automática completado exitosamente.");
        } catch (Exception e) {
            // Manejo de errores simple para desarrollo
            return ResponseEntity.internalServerError()
                    .body("Error al procesar la reasignación: " + e.getMessage());
        }
    }
}
