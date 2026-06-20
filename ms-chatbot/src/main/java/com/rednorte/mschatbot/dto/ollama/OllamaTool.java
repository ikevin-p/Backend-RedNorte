package com.rednorte.mschatbot.dto.ollama;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Definicion de una herramienta que el modelo puede decidir invocar.
 * Sigue el formato JSON Schema que espera Ollama (compatible con el
 * estandar de OpenAI function calling).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OllamaTool {
    private String type = "function";
    private Funcion function;

    public OllamaTool(Funcion function) {
        this.type = "function";
        this.function = function;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Funcion {
        private String name;
        private String description;
        private Map<String, Object> parameters;
    }
}
