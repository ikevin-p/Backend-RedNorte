package com.rednorte.notificaciones.service;

import com.rednorte.notificaciones.model.Notificacion;
import com.rednorte.notificaciones.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository repo;

    public Notificacion crear(Notificacion n) {
        return repo.save(n);
    }

    public List<Notificacion> listarPorUsuario(String usuarioId) {
        return repo.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }

    public List<Notificacion> listarNoLeidas(String usuarioId) {
        return repo.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(usuarioId);
    }

    public long contarNoLeidas(String usuarioId) {
        return repo.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    public Notificacion marcarLeida(Long id) {
        return repo.findById(id).map(n -> {
            n.setLeida(true);
            return repo.save(n);
        }).orElseThrow(() -> new RuntimeException("Notificacion no encontrada"));
    }

    public void marcarTodasLeidas(String usuarioId) {
        List<Notificacion> noLeidas = repo.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(usuarioId);
        noLeidas.forEach(n -> n.setLeida(true));
        repo.saveAll(noLeidas);
    }

    // Crear notificacion cuando cambia estado de consulta
    public Notificacion notificarCambioEstado(String usuarioId, Long consultaId, String estadoAnterior, String estadoNuevo) {
        String titulo = "Estado de consulta actualizado";
        String mensaje = String.format(
            "Tu consulta #%d ha cambiado de estado: %s → %s",
            consultaId, estadoAnterior, estadoNuevo
        );
        String tipo = switch (estadoNuevo) {
            case "AGENDADA"   -> "SUCCESS";
            case "ATENDIDA"   -> "SUCCESS";
            case "CANCELADA"  -> "ERROR";
            case "REASIGNADA" -> "WARNING";
            default           -> "INFO";
        };
        return repo.save(new Notificacion(usuarioId, titulo, mensaje, tipo, consultaId));
    }
}
