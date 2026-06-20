package com.rednorte.mschatbot.dto.ollama;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OllamaChatRequest {
    private String model;
    private List<OllamaMensaje> messages;
    private List<OllamaTool> tools;
    private boolean stream;
}
