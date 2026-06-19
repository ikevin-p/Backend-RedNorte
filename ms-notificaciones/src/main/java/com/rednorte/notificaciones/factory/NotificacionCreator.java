package com.rednorte.notificaciones.factory;

import com.rednorte.notificaciones.model.Notificacion;

/**
 * Patron Factory Method.
 *
 * Cada implementacion concreta sabe construir un tipo especifico de
 * Notificacion (titulo, mensaje y tipo visual), encapsulando la logica
 * de formato que antes vivia como un switch dentro de NotificacionService.
 *
 * Agregar un nuevo tipo de evento (por ejemplo "recordatorio de cita")
 * solo requiere crear una nueva clase que implemente esta interfaz y
 * registrarla en NotificacionFactory — no hay que tocar el switch
 * existente ni el resto del servicio.
 */
public interface NotificacionCreator {

    /**
     * Construye la notificacion concreta para este tipo de evento.
     *
     * @param usuarioId  destinatario de la notificacion
     * @param contexto   datos especificos del evento (ej. consultaId,
     *                   estadoAnterior, estadoNuevo) empaquetados en un
     *                   mapa simple para mantener la interfaz generica
     */
    Notificacion crear(String usuarioId, java.util.Map<String, Object> contexto);

    /**
     * Identifica que tipo de evento maneja esta factory concreta
     * (ej. "CAMBIO_ESTADO_CONSULTA", "REASIGNACION", "RECORDATORIO").
     */
    String tipoEvento();
}
