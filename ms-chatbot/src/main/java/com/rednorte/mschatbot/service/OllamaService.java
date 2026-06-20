package com.rednorte.mschatbot.service;

import com.rednorte.mschatbot.dto.ollama.OllamaChatRequest;
import com.rednorte.mschatbot.dto.ollama.OllamaChatResponse;
import com.rednorte.mschatbot.dto.ollama.OllamaMensaje;
import com.rednorte.mschatbot.dto.ollama.OllamaTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;

/**
 * Cliente HTTP hacia la API local de Ollama (POST /api/chat).
 *
 * Es deliberadamente delgado: solo sabe enviar un historial de mensajes
 * (mas las herramientas disponibles) y devolver la respuesta cruda del
 * modelo. La logica de QUE hacer con tool_calls vive en ChatbotService,
 * no aqui, para que este cliente sea reutilizable / facil de testear.
 */
@Service
public class OllamaService {

    private final WebClient webClient;
    private final String modelo;

    public OllamaService(
            @Value("${ollama.base-url}") String baseUrl,
            @Value("${ollama.modelo}") String modelo,
            @Value("${ollama.timeout-segundos:60}") long timeoutSegundos) {
        this.modelo = modelo;
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(timeoutSegundos));
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    /**
     * Envia el historial completo de la conversacion (incluyendo el
     * mensaje de sistema) y, opcionalmente, las herramientas disponibles.
     * Ollama decide solo, segun el contenido de la conversacion, si
     * responder texto plano o pedir invocar una herramienta.
     */
    public Mono<OllamaChatResponse> chat(List<OllamaMensaje> historial, List<OllamaTool> herramientas) {
        OllamaChatRequest request = new OllamaChatRequest(modelo, historial, herramientas, false);
        return webClient.post()
                .uri("/api/chat")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OllamaChatResponse.class);
    }
}
