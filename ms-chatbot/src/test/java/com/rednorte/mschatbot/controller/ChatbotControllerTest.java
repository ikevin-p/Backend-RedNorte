package com.rednorte.mschatbot.controller;

import com.rednorte.mschatbot.dto.MensajeRequestDTO;
import com.rednorte.mschatbot.dto.MensajeResponseDTO;
import com.rednorte.mschatbot.model.MensajeChatbot;
import com.rednorte.mschatbot.repository.MensajeChatbotRepository;
import com.rednorte.mschatbot.security.JwtUtilCompartido;
import com.rednorte.mschatbot.service.ChatbotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de la capa HTTP de ChatbotController.
 *
 * El endpoint POST /chatbot/mensaje es reactivo (Mono<MensajeResponseDTO>):
 * se prueba con WebTestClient.bindToController, que arma un servidor
 * reactivo minimo sin necesidad de levantar el contexto completo de
 * Spring ni filtros de seguridad. Los endpoints GET/DELETE de
 * historial son MVC normal y se prueban con MockMvc, igual que en el
 * resto de controllers del sistema.
 */
@WebMvcTest(ChatbotController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests HTTP — ChatbotController")
class ChatbotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatbotService chatbotService;

    @MockBean
    private MensajeChatbotRepository repository;

    @MockBean
    private JwtUtilCompartido jwtUtilCompartido;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(new ChatbotController(chatbotService, repository)).build();
    }

    @Test
    @DisplayName("POST /chatbot/mensaje con usuario logueado retorna la respuesta del servicio")
    void enviarMensaje_usuarioConSesion_retorna200ConRespuesta() {
        MensajeRequestDTO dto = new MensajeRequestDTO();
        dto.setMensaje("Hola");
        dto.setIdentificadorConversacion("conv-1");
        dto.setUsuarioId("USR010");
        dto.setNombrePaciente("Juan Pérez");

        var respuestaEsperada = new MensajeResponseDTO("¡Hola! ¿En qué puedo ayudarte?", null, null);
        respuestaEsperada.setEmocion("ACOGEDOR");
        when(chatbotService.procesarMensaje(any(MensajeRequestDTO.class))).thenReturn(Mono.just(respuestaEsperada));

        webTestClient.post().uri("/chatbot/mensaje")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.respuesta").isEqualTo("¡Hola! ¿En qué puedo ayudarte?")
                .jsonPath("$.emocion").isEqualTo("ACOGEDOR");
    }

    @Test
    @DisplayName("POST /chatbot/mensaje funciona tambien sin usuarioId (visitante anonimo)")
    void enviarMensaje_visitanteAnonimo_retorna200() {
        MensajeRequestDTO dto = new MensajeRequestDTO();
        dto.setMensaje("Hola, quiero info");
        dto.setIdentificadorConversacion("anon-uuid-123");
        // usuarioId queda null a proposito

        var respuestaEsperada = new MensajeResponseDTO("¡Hola! ¿En qué te puedo ayudar?", null, null);
        when(chatbotService.procesarMensaje(any(MensajeRequestDTO.class))).thenReturn(Mono.just(respuestaEsperada));

        webTestClient.post().uri("/chatbot/mensaje")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        verify(chatbotService).procesarMensaje(argThat(d -> d.getUsuarioId() == null));
    }

    @Test
    @DisplayName("POST /chatbot/mensaje sin texto de mensaje retorna 400")
    void enviarMensaje_sinTextoMensaje_retorna400() {
        MensajeRequestDTO dto = new MensajeRequestDTO();
        dto.setIdentificadorConversacion("conv-1");
        // mensaje queda null/vacio a proposito, viola @NotBlank

        webTestClient.post().uri("/chatbot/mensaje")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(chatbotService);
    }

    @Test
    @DisplayName("GET /chatbot/historial/{id} retorna el historial sin los mensajes de tipo tool")
    void historial_retorna200SinMensajesDeHerramientas() throws Exception {
        MensajeChatbot msgUsuario = new MensajeChatbot();
        msgUsuario.setRol("user");
        msgUsuario.setContenido("Hola");
        msgUsuario.setFechaHora(LocalDateTime.now());

        MensajeChatbot msgTool = new MensajeChatbot();
        msgTool.setRol("tool");
        msgTool.setContenido("{\"exito\":true}");
        msgTool.setFechaHora(LocalDateTime.now());

        MensajeChatbot msgAsistente = new MensajeChatbot();
        msgAsistente.setRol("assistant");
        msgAsistente.setContenido("¡Hola! ¿En qué te ayudo?");
        msgAsistente.setFechaHora(LocalDateTime.now());

        when(repository.findByIdentificadorConversacionOrderByFechaHoraAsc("conv-1"))
                .thenReturn(List.of(msgUsuario, msgTool, msgAsistente));

        mockMvc.perform(get("/chatbot/historial/conv-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].rol").value("user"))
                .andExpect(jsonPath("$[1].rol").value("assistant"));
    }

    @Test
    @DisplayName("GET /chatbot/historial/{id} de una conversacion sin mensajes retorna lista vacia")
    void historial_conversacionSinMensajes_retornaListaVacia() throws Exception {
        when(repository.findByIdentificadorConversacionOrderByFechaHoraAsc("conv-nueva")).thenReturn(List.of());

        mockMvc.perform(get("/chatbot/historial/conv-nueva"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("DELETE /chatbot/historial/{id} borra el historial y retorna 204")
    void borrarHistorial_idValido_retorna204() throws Exception {
        doNothing().when(repository).deleteByIdentificadorConversacion("conv-1");

        mockMvc.perform(delete("/chatbot/historial/conv-1"))
                .andExpect(status().isNoContent());

        verify(repository).deleteByIdentificadorConversacion("conv-1");
    }
}
