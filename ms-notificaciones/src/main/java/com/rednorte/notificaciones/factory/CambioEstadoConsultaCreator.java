package com.rednorte.notificaciones.factory;

import com.rednorte.notificaciones.model.Notificacion;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Factory Method concreto: construye la notificacion para un cambio de
 * estado de una consulta medica (PENDIENTE -> AGENDADA -> ATENDIDA, o
 * CANCELADA/REASIGNADA).
 */
@Component
public class CambioEstadoConsultaCreator implements NotificacionCreator {

    @Override
    public String tipoEvento() {
        return "CAMBIO_ESTADO_CONSULTA";
    }

    @Override
    public Notificacion crear(String usuarioId, Map<String, Object> contexto) {
        Long consultaId = (Long) contexto.get("consultaId");
        String estadoAnterior = (String) contexto.get("estadoAnterior");
        String estadoNuevo = (String) contexto.get("estadoNuevo");

        String titulo = "Estado de consulta actualizado";
        String mensaje = String.format(
            "Tu consulta #%d ha cambiado de estado: %s → %s",
            consultaId, estadoAnterior, estadoNuevo
        );
        String tipoVisual = switch (estadoNuevo) {
            case "AGENDADA", "ATENDIDA" -> "SUCCESS";
            case "CANCELADA"            -> "ERROR";
            case "REASIGNADA"           -> "WARNING";
            default                     -> "INFO";
        };

        return new Notificacion(usuarioId, titulo, mensaje, tipoVisual, consultaId);
    }
}
