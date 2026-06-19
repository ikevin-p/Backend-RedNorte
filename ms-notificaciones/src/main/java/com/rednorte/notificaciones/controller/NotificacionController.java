package com.rednorte.notificaciones.controller;

import com.rednorte.notificaciones.model.Notificacion;
import com.rednorte.notificaciones.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notificaciones")
// NOTA: el CORS lo maneja exclusivamente el API Gateway (globalcors).
public class NotificacionController {

    @Autowired
    private NotificacionService service;

    // Crear notificacion manual
    @PostMapping
    public ResponseEntity<Notificacion> crear(@RequestBody Notificacion n) {
        return ResponseEntity.ok(service.crear(n));
    }

    // Notificar cambio de estado de consulta
    @PostMapping("/cambio-estado")
    public ResponseEntity<Notificacion> cambioEstado(@RequestBody Map<String, Object> body) {
        String usuarioId   = (String) body.get("usuarioId");
        Long consultaId    = Long.valueOf(body.get("consultaId").toString());
        String estadoAntes = (String) body.get("estadoAnterior");
        String estadoDes   = (String) body.get("estadoNuevo");
        return ResponseEntity.ok(service.notificarCambioEstado(usuarioId, consultaId, estadoAntes, estadoDes));
    }

    // Listar todas las notificaciones de un usuario
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Notificacion>> listar(@PathVariable String usuarioId) {
        return ResponseEntity.ok(service.listarPorUsuario(usuarioId));
    }

    // Listar solo las no leidas
    @GetMapping("/usuario/{usuarioId}/no-leidas")
    public ResponseEntity<List<Notificacion>> noLeidas(@PathVariable String usuarioId) {
        return ResponseEntity.ok(service.listarNoLeidas(usuarioId));
    }

    // Contar no leidas (para el badge de la campana)
    @GetMapping("/usuario/{usuarioId}/contador")
    public ResponseEntity<Map<String, Long>> contador(@PathVariable String usuarioId) {
        return ResponseEntity.ok(Map.of("noLeidas", service.contarNoLeidas(usuarioId)));
    }

    // Marcar una como leida
    @PutMapping("/{id}/leer")
    public ResponseEntity<Notificacion> marcarLeida(@PathVariable Long id) {
        return ResponseEntity.ok(service.marcarLeida(id));
    }

    // Marcar todas como leidas
    @PutMapping("/usuario/{usuarioId}/leer-todas")
    public ResponseEntity<Void> marcarTodasLeidas(@PathVariable String usuarioId) {
        service.marcarTodasLeidas(usuarioId);
        return ResponseEntity.ok().build();
    }
}
