package com.rednorte.mschatbot.dto.ollama;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OllamaChatResponse {
    private String model;
    private OllamaMensaje message;
    private boolean done;
}
