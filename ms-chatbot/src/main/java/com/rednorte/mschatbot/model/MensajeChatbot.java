package com.rednorte.mschatbot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Un mensaje individual dentro de una conversacion con SaludBot.
 *
 * La conversacion completa se reconstruye agrupando todas las filas
 * que comparten el mismo identificadorConversacion, ordenadas por
 * fechaHora. Se modela asi (fila por mensaje) en vez de un blob JSON
 * para poder auditar/consultar mensajes individuales facilmente.
 */
@Entity
@Table(name = "mensaje_chatbot")
@Data
@NoArgsConstructor
public class MensajeChatbot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Agrupa todos los mensajes de una misma conversacion. Si el usuario
    // tiene sesion, es su usuarioId; si es anonimo, es un UUID generado
    // por el frontend y guardado en localStorage.
    @Column(nullable = false)
    private String identificadorConversacion;

    // Null si la conversacion es de un visitante anonimo.
    private String usuarioId;

    @Column(nullable = false)
    private String rol; // "user" | "assistant" | "tool"

    @Column(nullable = false, length = 4000)
    private String contenido;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @PrePersist
    void antesDeGuardar() {
        if (fechaHora == null) fechaHora = LocalDateTime.now();
    }
}
