package com.rednorte.msagendamedica.controller;

import com.rednorte.msagendamedica.model.BloqueAgenda;
import com.rednorte.msagendamedica.service.AgendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agenda")
// NOTA: el CORS lo maneja exclusivamente el API Gateway (globalcors).
@Tag(name = "Agenda Médica", description = "Gestión de bloques horarios para doctores")
public class AgendaController {

    private final AgendaService service;

    public AgendaController(AgendaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todos los bloques")
    public List<BloqueAgenda> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener bloque por ID")
    public ResponseEntity<BloqueAgenda> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Obtener agenda de un doctor")
    public List<BloqueAgenda> porDoctor(@PathVariable String doctorId) {
        return service.buscarPorDoctor(doctorId);
    }

    @GetMapping("/doctor/{doctorId}/fecha/{fecha}")
    @Operation(summary = "Obtener agenda de un doctor en una fecha específica")
    public List<BloqueAgenda> porDoctorYFecha(
            @PathVariable String doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return service.buscarPorDoctorYFecha(doctorId, fecha);
    }

    @GetMapping("/disponibles/{fecha}")
    @Operation(summary = "Listar bloques disponibles por fecha")
    public List<BloqueAgenda> disponiblesPorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return service.buscarDisponiblesPorFecha(fecha);
    }

    @GetMapping("/paciente/{pacienteId}")
    @Operation(summary = "Obtener citas reservadas de un paciente")
    public List<BloqueAgenda> porPaciente(@PathVariable String pacienteId) {
        return service.buscarPorPaciente(pacienteId);
    }

    @PostMapping
    @Operation(summary = "Crear un bloque manualmente")
    public ResponseEntity<BloqueAgenda> crear(@RequestBody BloqueAgenda bloque) {
        return ResponseEntity.ok(service.crear(bloque));
    }

    @PostMapping("/generar")
    @Operation(summary = "Generar bloques de 30 min automáticamente (08:00-17:00)")
    public ResponseEntity<List<BloqueAgenda>> generarBloques(@RequestBody Map<String, String> body) {
        String doctorId = body.get("doctorId");
        String establecimientoId = body.get("establecimientoId");
        LocalDate fecha = LocalDate.parse(body.get("fecha"));
        return ResponseEntity.ok(service.generarBloques(doctorId, establecimientoId, fecha));
    }

    @PutMapping("/{id}/reservar")
    @Operation(summary = "Reservar un bloque para un paciente")
    public ResponseEntity<BloqueAgenda> reservar(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            return service.reservar(id, body.get("pacienteId"), body.get("consultaId"))
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar un bloque reservado")
    public ResponseEntity<BloqueAgenda> cancelar(@PathVariable Long id) {
        return service.cancelar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/completar")
    @Operation(summary = "Marcar un bloque como completado")
    public ResponseEntity<BloqueAgenda> completar(@PathVariable Long id) {
        return service.completar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un bloque")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
