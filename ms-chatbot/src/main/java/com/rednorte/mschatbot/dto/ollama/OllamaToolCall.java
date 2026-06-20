package com.rednorte.mschatbot.dto.ollama;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/** Lo que Ollama devuelve cuando el modelo decide invocar una herramienta. */
@Data
@NoArgsConstructor
public class OllamaToolCall {
    private String id;
    private Funcion function;

    @Data
    @NoArgsConstructor
    public static class Funcion {
        private String name;
        private Map<String, Object> arguments;
    }
}
