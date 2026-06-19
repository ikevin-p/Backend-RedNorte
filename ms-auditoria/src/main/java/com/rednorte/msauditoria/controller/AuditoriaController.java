package com.rednorte.msauditoria.controller;

import com.rednorte.msauditoria.model.RegistroAuditoria;
import com.rednorte.msauditoria.service.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/auditoria")
// NOTA: el CORS lo maneja exclusivamente el API Gateway (globalcors).
@Tag(name = "Auditoría", description = "Trazabilidad de acciones del sistema")
public class AuditoriaController {

    private final AuditoriaService service;

    public AuditoriaController(AuditoriaService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Registrar una acción en auditoría")
    public ResponseEntity<RegistroAuditoria> registrar(@RequestBody RegistroAuditoria registro) {
        return ResponseEntity.ok(service.registrar(registro));
    }

    @GetMapping
    @Operation(summary = "Listar los 100 registros más recientes")
    public List<RegistroAuditoria> listarRecientes() {
        return service.listarRecientes();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener registro por ID")
    public ResponseEntity<RegistroAuditoria> porId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Historial de acciones de un usuario")
    public List<RegistroAuditoria> porUsuario(@PathVariable String usuarioId) {
        return service.porUsuario(usuarioId);
    }

    @GetMapping("/modulo/{modulo}")
    @Operation(summary = "Registros por módulo (USUARIOS, CONSULTAS, FICHA_MEDICA, AGENDA, ESTABLECIMIENTOS, NOTIFICACIONES, REASIGNACION)")
    public ResponseEntity<List<RegistroAuditoria>> porModulo(@PathVariable String modulo) {
        try {
            return ResponseEntity.ok(service.porModulo(modulo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/accion/{accion}")
    @Operation(summary = "Registros por tipo de acción (LOGIN, CREAR, ACTUALIZAR, ELIMINAR, RESERVAR, CANCELAR, etc.)")
    public ResponseEntity<List<RegistroAuditoria>> porAccion(@PathVariable String accion) {
        try {
            return ResponseEntity.ok(service.porAccion(accion));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/resultado/{resultado}")
    @Operation(summary = "Registros por resultado (EXITOSO, FALLIDO, RECHAZADO)")
    public ResponseEntity<List<RegistroAuditoria>> porResultado(@PathVariable String resultado) {
        try {
            return ResponseEntity.ok(service.porResultado(resultado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/rango")
    @Operation(summary = "Registros en un rango de fechas")
    public List<RegistroAuditoria> porRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return service.porRango(desde, hasta);
    }
}
