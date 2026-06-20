package com.rednorte.notificaciones.controller;

import com.rednorte.notificaciones.model.Notificacion;
import com.rednorte.notificaciones.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notificaciones")
// NOTA: el CORS lo maneja exclusivamente el API Gateway (globalcors).
@Tag(name = "Notificaciones", description = "Notificaciones al paciente por cambios de estado y reasignaciones")
public class NotificacionController {

    @Autowired
    private NotificacionService service;

    // Crear notificacion manual
    @PostMapping
    @Operation(summary = "Crear notificación manual")
    public ResponseEntity<Notificacion> crear(@RequestBody Notificacion n) {
        return ResponseEntity.ok(service.crear(n));
    }

    // Notificar cambio de estado de consulta
    @PostMapping("/cambio-estado")
    @Operation(summary = "Notificar cambio de estado de consulta", description = "Usa el patrón Factory Method (CambioEstadoConsultaCreator) para construir el mensaje según el nuevo estado")
    public ResponseEntity<Notificacion> cambioEstado(@RequestBody Map<String, Object> body) {
        String usuarioId   = (String) body.get("usuarioId");
        Long consultaId    = Long.valueOf(body.get("consultaId").toString());
        String estadoAntes = (String) body.get("estadoAnterior");
        String estadoDes   = (String) body.get("estadoNuevo");
        return ResponseEntity.ok(service.notificarCambioEstado(usuarioId, consultaId, estadoAntes, estadoDes));
    }

    // Notificar a un paciente que fue reasignado automaticamente a un
    // cupo liberado (llamado desde ms-reasignacion -> ReasignacionService).
    @PostMapping("/reasignacion")
    @Operation(summary = "Notificar reasignación automática", description = "Uso interno de ms-reasignacion: usa el patrón Factory Method (ReasignacionCreator)")
    public ResponseEntity<Notificacion> reasignacion(@RequestBody Map<String, Object> body) {
        String usuarioId    = (String) body.get("usuarioId");
        Long consultaId     = Long.valueOf(body.get("consultaId").toString());
        String especialidad = (String) body.get("especialidad");
        return ResponseEntity.ok(service.notificarReasignacion(usuarioId, consultaId, especialidad));
    }

    // Listar todas las notificaciones de un usuario
    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar notificaciones de un usuario")
    public ResponseEntity<List<Notificacion>> listar(@PathVariable String usuarioId) {
        return ResponseEntity.ok(service.listarPorUsuario(usuarioId));
    }

    // Listar solo las no leidas
    @GetMapping("/usuario/{usuarioId}/no-leidas")
    @Operation(summary = "Listar notificaciones no leídas")
    public ResponseEntity<List<Notificacion>> noLeidas(@PathVariable String usuarioId) {
        return ResponseEntity.ok(service.listarNoLeidas(usuarioId));
    }

    // Contar no leidas (para el badge de la campana)
    @GetMapping("/usuario/{usuarioId}/contador")
    @Operation(summary = "Contar notificaciones no leídas", description = "Usado para el badge numérico de la campana de notificaciones")
    public ResponseEntity<Map<String, Long>> contador(@PathVariable String usuarioId) {
        return ResponseEntity.ok(Map.of("noLeidas", service.contarNoLeidas(usuarioId)));
    }

    // Marcar una como leida
    @PutMapping("/{id}/leer")
    @Operation(summary = "Marcar notificación como leída")
    public ResponseEntity<Notificacion> marcarLeida(@PathVariable Long id) {
        return ResponseEntity.ok(service.marcarLeida(id));
    }

    // Marcar todas como leidas
    @PutMapping("/usuario/{usuarioId}/leer-todas")
    @Operation(summary = "Marcar todas las notificaciones como leídas")
    public ResponseEntity<Void> marcarTodasLeidas(@PathVariable String usuarioId) {
        service.marcarTodasLeidas(usuarioId);
        return ResponseEntity.ok().build();
    }
}
