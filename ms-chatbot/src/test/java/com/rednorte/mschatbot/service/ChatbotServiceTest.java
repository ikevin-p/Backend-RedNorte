package com.rednorte.mschatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rednorte.mschatbot.dto.MensajeRequestDTO;
import com.rednorte.mschatbot.dto.ollama.OllamaChatResponse;
import com.rednorte.mschatbot.dto.ollama.OllamaMensaje;
import com.rednorte.mschatbot.dto.ollama.OllamaToolCall;
import com.rednorte.mschatbot.model.MensajeChatbot;
import com.rednorte.mschatbot.repository.MensajeChatbotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios — ChatbotService")
class ChatbotServiceTest {

    @Mock
    private OllamaService ollamaService;
    @Mock
    private HerramientasChatbot herramientas;
    @Mock
    private MensajeChatbotRepository repository;

    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        chatbotService = new ChatbotService(ollamaService, herramientas, repository, new ObjectMapper(), new DetectorEmocion());
        lenient().when(herramientas.definiciones()).thenReturn(List.of());
    }

    private OllamaChatResponse respuestaTexto(String texto) {
        var msg = new OllamaMensaje();
        msg.setRole("assistant");
        msg.setContent(texto);
        var resp = new OllamaChatResponse();
        resp.setMessage(msg);
        resp.setDone(true);
        return resp;
    }

    private OllamaChatResponse respuestaConToolCall(String nombreHerramienta, Map<String, Object> argumentos) {
        var funcion = new OllamaToolCall.Funcion();
        funcion.setName(nombreHerramienta);
        funcion.setArguments(argumentos);
        var toolCall = new OllamaToolCall();
        toolCall.setFunction(funcion);

        var msg = new OllamaMensaje();
        msg.setRole("assistant");
        msg.setContent("");
        msg.setToolCalls(List.of(toolCall));

        var resp = new OllamaChatResponse();
        resp.setMessage(msg);
        return resp;
    }

    @Test
    @DisplayName("Mensaje sin tool_calls: persiste el mensaje del usuario y la respuesta, devuelve el texto")
    void procesarMensaje_sinToolCalls_devuelveRespuestaDirecta() {
        when(repository.findByIdentificadorConversacionOrderByFechaHoraAsc("conv-1")).thenReturn(List.of());
        when(ollamaService.chat(anyList(), anyList())).thenReturn(Mono.just(respuestaTexto("¡Hola! ¿En qué puedo ayudarte?")));

        MensajeRequestDTO dto = new MensajeRequestDTO();
        dto.setMensaje("Hola");
        dto.setIdentificadorConversacion("conv-1");
        dto.setUsuarioId("USR010");
        dto.setNombrePaciente("Juan Pérez");

        StepVerifier.create(chatbotService.procesarMensaje(dto))
                .assertNext(resp -> {
                    org.junit.jupiter.api.Assertions.assertEquals("¡Hola! ¿En qué puedo ayudarte?", resp.getRespuesta());
                    org.junit.jupiter.api.Assertions.assertNull(resp.getAccionRealizada());
                })
                .verifyComplete();

        verify(repository, times(2)).save(any(MensajeChatbot.class)); // mensaje user + respuesta assistant
    }

    @Test
    @DisplayName("Primer turno de la conversacion (historial vacio) detecta emocion ACOGEDOR")
    void procesarMensaje_primerTurno_emocionAcogedor() {
        when(repository.findByIdentificadorConversacionOrderByFechaHoraAsc("conv-nueva")).thenReturn(List.of());
        when(ollamaService.chat(anyList(), anyList())).thenReturn(Mono.just(respuestaTexto("¡Bienvenido a RedNorte!")));

        MensajeRequestDTO dto = new MensajeRequestDTO();
        dto.setMensaje("Hola");
        dto.setIdentificadorConversacion("conv-nueva");
        dto.setUsuarioId("USR010");

        StepVerifier.create(chatbotService.procesarMensaje(dto))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertEquals(DetectorEmocion.ACOGEDOR, resp.getEmocion()))
                .verifyComplete();
    }

    @Test
    @DisplayName("El modelo pide una herramienta, se ejecuta y el resultado se reinyecta antes de la respuesta final")
    void procesarMensaje_conToolCall_ejecutaHerramientaYContinua() {
        when(repository.findByIdentificadorConversacionOrderByFechaHoraAsc("conv-2"))
                .thenReturn(List.of()) // antes de guardar el mensaje del usuario
                .thenReturn(List.of(mensajeGuardado("user", "Quiero ver horarios del 22 de junio")));

        when(ollamaService.chat(anyList(), anyList()))
                .thenReturn(Mono.just(respuestaConToolCall("buscar_horarios_disponibles", Map.of("fecha", "2026-06-22"))))
                .thenReturn(Mono.just(respuestaTexto("Tengo disponible a las 08:00 con el Dr. Vega.")));

        when(herramientas.ejecutar(eq("buscar_horarios_disponibles"), any(), eq("USR010"), any()))
                .thenReturn(Mono.just("[{\"bloqueId\":501,\"hora\":\"08:00\",\"especialidad\":\"Cardiología\"}]"));

        MensajeRequestDTO dto = new MensajeRequestDTO();
        dto.setMensaje("Quiero ver horarios del 22 de junio");
        dto.setIdentificadorConversacion("conv-2");
        dto.setUsuarioId("USR010");

        StepVerifier.create(chatbotService.procesarMensaje(dto))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertEquals(
                        "Tengo disponible a las 08:00 con el Dr. Vega.", resp.getRespuesta()))
                .verifyComplete();

        verify(herramientas).ejecutar(eq("buscar_horarios_disponibles"), any(), eq("USR010"), any());
        // Se guarda: mensaje del usuario + resultado de la herramienta (tool) + respuesta final
        verify(repository, times(3)).save(any(MensajeChatbot.class));
    }

    @Test
    @DisplayName("crear_cita_real exitosa marca accionRealizada=CITA_AGENDADA y fuerza emocion CELEBRACION")
    void procesarMensaje_citaAgendadaExitosamente_marcaAccionYCelebracion() {
        when(repository.findByIdentificadorConversacionOrderByFechaHoraAsc("conv-3")).thenReturn(List.of());

        when(ollamaService.chat(anyList(), anyList()))
                .thenReturn(Mono.just(respuestaConToolCall("crear_cita_real", Map.of(
                        "bloqueId", 501, "nombrePaciente", "Juan Pérez", "rut", "11111111-1", "motivoConsulta", "Control"))))
                .thenReturn(Mono.just(respuestaTexto("¡Listo! Tu cita quedó agendada para el 22 de junio a las 08:00.")));

        when(herramientas.ejecutar(eq("crear_cita_real"), any(), eq("USR010"), any()))
                .thenReturn(Mono.just("{\"exito\":true,\"consultaId\":999,\"fecha\":\"2026-06-22\",\"hora\":\"08:00\"}"));

        MensajeRequestDTO dto = new MensajeRequestDTO();
        dto.setMensaje("Sí, confirmo esa hora");
        dto.setIdentificadorConversacion("conv-3");
        dto.setUsuarioId("USR010");
        dto.setNombrePaciente("Juan Pérez");

        StepVerifier.create(chatbotService.procesarMensaje(dto))
                .assertNext(resp -> {
                    org.junit.jupiter.api.Assertions.assertEquals("CITA_AGENDADA", resp.getAccionRealizada());
                    org.junit.jupiter.api.Assertions.assertEquals(DetectorEmocion.CELEBRACION, resp.getEmocion());
                    org.junit.jupiter.api.Assertions.assertNotNull(resp.getDatosAccion());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("crear_cita_real para visitante anonimo marca accionRealizada=REDIRIGIR_REGISTRO")
    void procesarMensaje_visitanteAnonimoQuiereAgendar_marcaRedirigirRegistro() {
        when(repository.findByIdentificadorConversacionOrderByFechaHoraAsc("conv-4")).thenReturn(List.of());

        when(ollamaService.chat(anyList(), anyList()))
                .thenReturn(Mono.just(respuestaConToolCall("crear_cita_real", Map.of(
                        "bloqueId", 501, "nombrePaciente", "Anónimo", "rut", "11111111-1", "motivoConsulta", "Control"))))
                .thenReturn(Mono.just(respuestaTexto("Para agendar necesitas crear una cuenta primero.")));

        when(herramientas.ejecutar(eq("crear_cita_real"), any(), isNull(), any()))
                .thenReturn(Mono.just("{\"exito\": false, \"requiereRegistro\": true}"));

        MensajeRequestDTO dto = new MensajeRequestDTO();
        dto.setMensaje("Quiero agendar una hora");
        dto.setIdentificadorConversacion("conv-4");
        dto.setUsuarioId(null);

        StepVerifier.create(chatbotService.procesarMensaje(dto))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertEquals("REDIRIGIR_REGISTRO", resp.getAccionRealizada()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Si una herramienta falla (exito=false), la siguiente respuesta se marca con emocion CONFUNDIDO")
    void procesarMensaje_herramientaFalla_emocionConfundido() {
        when(repository.findByIdentificadorConversacionOrderByFechaHoraAsc("conv-5")).thenReturn(List.of());

        when(ollamaService.chat(anyList(), anyList()))
                .thenReturn(Mono.just(respuestaConToolCall("crear_cita_real", Map.of(
                        "bloqueId", 501, "nombrePaciente", "Juan", "rut", "x", "motivoConsulta", "Control"))))
                .thenReturn(Mono.just(respuestaTexto("Hubo un problema, ¿probamos con otro horario?")));

        when(herramientas.ejecutar(eq("crear_cita_real"), any(), eq("USR010"), any()))
                .thenReturn(Mono.just("{\"exito\": false, \"error\": \"Bloque ya no disponible\"}"));

        MensajeRequestDTO dto = new MensajeRequestDTO();
        dto.setMensaje("Confirmo esa hora");
        dto.setIdentificadorConversacion("conv-5");
        dto.setUsuarioId("USR010");

        StepVerifier.create(chatbotService.procesarMensaje(dto))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertEquals(DetectorEmocion.CONFUNDIDO, resp.getEmocion()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Si el modelo insiste en pedir herramientas mas alla del limite, finaliza con texto igual")
    void procesarMensaje_excedeLimiteDeVueltas_finalizaIgual() {
        when(repository.findByIdentificadorConversacionOrderByFechaHoraAsc("conv-6")).thenReturn(List.of());

        // El modelo siempre pide la misma herramienta, nunca da texto final por si solo.
        when(ollamaService.chat(anyList(), anyList()))
                .thenReturn(Mono.just(respuestaConToolCall("buscar_horarios_disponibles", Map.of("fecha", "2026-06-22"))));
        when(herramientas.ejecutar(any(), any(), any(), any()))
                .thenReturn(Mono.just("[]"));

        MensajeRequestDTO dto = new MensajeRequestDTO();
        dto.setMensaje("horarios");
        dto.setIdentificadorConversacion("conv-6");
        dto.setUsuarioId("USR010");

        StepVerifier.create(chatbotService.procesarMensaje(dto))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertNotNull(resp.getRespuesta()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Si el modelo responde texto vacio sin tool_calls, se usa un mensaje de respaldo")
    void procesarMensaje_respuestaVacia_usaMensajeDeRespaldo() {
        when(repository.findByIdentificadorConversacionOrderByFechaHoraAsc("conv-7")).thenReturn(List.of());
        when(ollamaService.chat(anyList(), anyList())).thenReturn(Mono.just(respuestaTexto("")));

        MensajeRequestDTO dto = new MensajeRequestDTO();
        dto.setMensaje("???");
        dto.setIdentificadorConversacion("conv-7");
        dto.setUsuarioId("USR010");

        StepVerifier.create(chatbotService.procesarMensaje(dto))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertTrue(resp.getRespuesta().contains("reformularlo")))
                .verifyComplete();
    }

    private MensajeChatbot mensajeGuardado(String rol, String contenido) {
        var m = new MensajeChatbot();
        m.setRol(rol);
        m.setContenido(contenido);
        return m;
    }

    private <T> T isNull() {
        return org.mockito.ArgumentMatchers.isNull();
    }

    private <T> T eq(T valor) {
        return org.mockito.ArgumentMatchers.eq(valor);
    }
}
