package com.rednorte.mschatbot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lo que envia el widget de chat al backend.
 *
 * usuarioId y nombrePaciente llegan null si la persona no tiene sesion
 * iniciada (visitante anonimo); en ese caso, identificadorConversacion
 * es el UUID que el frontend genero y guarda en localStorage.
 */
@Data
@NoArgsConstructor
public class MensajeRequestDTO {

    @NotBlank(message = "El mensaje no puede estar vacío")
    private String mensaje;

    @NotBlank(message = "El identificador de conversación es obligatorio")
    private String identificadorConversacion;

    private String usuarioId;
    private String nombrePaciente;
}
