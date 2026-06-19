package com.rednorte.notificaciones.factory;

import com.rednorte.notificaciones.model.Notificacion;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Factory Method concreto: construye la notificacion cuando una cita
 * cancelada es reasignada automaticamente a un paciente de la lista
 * de espera (ver Backend_reasignacion -> ReasignacionService).
 */
@Component
public class ReasignacionCreator implements NotificacionCreator {

    @Override
    public String tipoEvento() {
        return "REASIGNACION";
    }

    @Override
    public Notificacion crear(String usuarioId, Map<String, Object> contexto) {
        Long consultaId = (Long) contexto.get("consultaId");
        String especialidad = (String) contexto.getOrDefault("especialidad", "tu especialidad");

        String titulo = "¡Te asignamos una hora disponible!";
        String mensaje = String.format(
            "Se liberó un cupo de %s y fuiste reasignado automáticamente. Revisa el detalle en tu consulta #%d.",
            especialidad, consultaId
        );

        return new Notificacion(usuarioId, titulo, mensaje, "SUCCESS", consultaId);
    }
}
