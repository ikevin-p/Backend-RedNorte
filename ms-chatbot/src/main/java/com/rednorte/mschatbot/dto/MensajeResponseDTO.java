package com.rednorte.mschatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lo que el backend devuelve al widget tras procesar un mensaje.
 *
 * accionRealizada distingue, para la UI, si la respuesta es solo
 * conversacional o si SaludBot detecto que el paciente necesita un
 * formulario visual (registro, login, recuperar contraseña o agendar
 * una cita), de modo que el frontend abra el modal correspondiente en
 * vez de mostrar solo texto. La confirmacion visual de una cita ya
 * agendada ("CITA_AGENDADA") ya no la produce este backend: ocurre
 * enteramente en el navegador cuando el modal de agendamiento reserva
 * el bloque (ver AgendarCitaModal.jsx / ChatbotWidget.jsx), asi que el
 * frontend la sintetiza localmente con esta misma forma de dato.
 *
 * emocion le dice al widget que expresion mostrar en el avatar del
 * robot junto a esta respuesta especifica (ver DetectorEmocion).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensajeResponseDTO {
    private String respuesta;
    private String accionRealizada; // null | "REDIRIGIR_REGISTRO" | "REDIRIGIR_LOGIN" | "REDIRIGIR_RECUPERAR" | "REDIRIGIR_AGENDAR"
    private Object datosAccion;     // reservado para futuras acciones con detalle adicional
    private String emocion;         // ACOGEDOR | CONCENTRADO | CELEBRACION | CONFUNDIDO | EMPATICO | ALERTA | DESPEDIDA

    public MensajeResponseDTO(String respuesta, String accionRealizada, Object datosAccion) {
        this.respuesta = respuesta;
        this.accionRealizada = accionRealizada;
        this.datosAccion = datosAccion;
    }
}

