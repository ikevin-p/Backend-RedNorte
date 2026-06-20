package com.rednorte.mschatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lo que el backend devuelve al widget tras procesar un mensaje.
 *
 * accionRealizada distingue, para la UI, si la respuesta es solo
 * conversacional o si efectivamente paso algo (ej. "CITA_AGENDADA"),
 * de modo que el frontend pueda mostrar una confirmacion visual
 * especial (no solo texto) cuando corresponda.
 *
 * emocion le dice al widget que expresion mostrar en el avatar del
 * robot junto a esta respuesta especifica (ver DetectorEmocion).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensajeResponseDTO {
    private String respuesta;
    private String accionRealizada; // null | "CITA_AGENDADA" | "REDIRIGIR_REGISTRO" | "REDIRIGIR_LOGIN"
    private Object datosAccion;     // detalle de la cita agendada, si aplica
    private String emocion;         // ACOGEDOR | CONCENTRADO | CELEBRACION | CONFUNDIDO | EMPATICO | ALERTA | DESPEDIDA

    public MensajeResponseDTO(String respuesta, String accionRealizada, Object datosAccion) {
        this.respuesta = respuesta;
        this.accionRealizada = accionRealizada;
        this.datosAccion = datosAccion;
    }
}

