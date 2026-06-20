package com.rednorte.mschatbot.dto.ollama;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Un mensaje dentro del historial enviado a Ollama.
 * role: "system" | "user" | "assistant" | "tool"
 * toolCallId/name solo se usan cuando role="tool" (resultado de una herramienta).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OllamaMensaje {
    private String role;
    private String content;

    @JsonProperty("tool_calls")
    private List<OllamaToolCall> toolCalls;

    public static OllamaMensaje sistema(String contenido) {
        return new OllamaMensaje("system", contenido, null);
    }

    public static OllamaMensaje usuario(String contenido) {
        return new OllamaMensaje("user", contenido, null);
    }

    public static OllamaMensaje asistente(String contenido) {
        return new OllamaMensaje("assistant", contenido, null);
    }

    public static OllamaMensaje resultadoHerramienta(String contenidoJson) {
        return new OllamaMensaje("tool", contenidoJson, null);
    }
}
