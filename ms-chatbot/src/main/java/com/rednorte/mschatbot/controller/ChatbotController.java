package com.rednorte.mschatbot.controller;

import com.rednorte.mschatbot.dto.MensajeRequestDTO;
import com.rednorte.mschatbot.dto.MensajeResponseDTO;
import com.rednorte.mschatbot.repository.MensajeChatbotRepository;
import com.rednorte.mschatbot.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/chatbot")
@Tag(name = "Chatbot", description = "SaludBot: asistente virtual con Ollama/Llama 3.2")
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final MensajeChatbotRepository repository;

    public ChatbotController(ChatbotService chatbotService, MensajeChatbotRepository repository) {
        this.chatbotService = chatbotService;
        this.repository = repository;
    }

    @PostMapping("/mensaje")
    @Operation(summary = "Envía un mensaje a SaludBot",
            description = "Disponible para usuarios con sesión y para visitantes anónimos. " +
                    "Si usuarioId viene null, se trata como visitante sin cuenta.")
    public Mono<MensajeResponseDTO> enviarMensaje(@Valid @RequestBody MensajeRequestDTO dto) {
        return chatbotService.procesarMensaje(dto);
    }

    @GetMapping("/historial/{identificadorConversacion}")
    @Operation(summary = "Obtiene el historial de una conversación (para restaurarla al recargar la página)")
    public ResponseEntity<?> historial(@PathVariable String identificadorConversacion) {
        var mensajes = repository.findByIdentificadorConversacionOrderByFechaHoraAsc(identificadorConversacion);
        return ResponseEntity.ok(mensajes.stream()
                .filter(m -> !"tool".equals(m.getRol())) // los resultados de herramientas son detalle interno
                .map(m -> new HistorialItem(m.getRol(), m.getContenido(), m.getFechaHora()))
                .toList());
    }

    @DeleteMapping("/historial/{identificadorConversacion}")
    @Operation(summary = "Borra el historial de una conversación (botón 'nueva conversación' del widget)")
    public ResponseEntity<Void> borrarHistorial(@PathVariable String identificadorConversacion) {
        repository.deleteByIdentificadorConversacion(identificadorConversacion);
        return ResponseEntity.noContent().build();
    }

    record HistorialItem(String rol, String contenido, java.time.LocalDateTime fechaHora) {}
}
