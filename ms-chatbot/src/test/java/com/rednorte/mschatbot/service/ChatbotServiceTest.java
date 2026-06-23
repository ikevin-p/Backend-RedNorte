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
import org.mockito.ArgumentCaptor;
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
                .thenReturn(List.of(mensajeGuardado("user", "No puedo entrar a mi cuenta")));

        when(ollamaService.chat(anyList(), anyList()))
                .thenReturn(Mono.just(respuestaConToolCall("iniciar_flujo_ui", Map.of("tipo", "LOGIN"))))
                .thenReturn(Mono.just(respuestaTexto("Te abrí el formulario de inicio de sesión.")));

        when(herramientas.ejecutar(eq("iniciar_flujo_ui"), any(), eq("USR010"), any()))
                .thenReturn(Mono.just("{\"exito\": true, \"tipoFormulario\": \"LOGIN\"}"));

        MensajeRequestDTO dto = new MensajeRequestDTO();
        dto.setMensaje("No puedo entrar a mi cuenta");
        dto.setIdentificadorConversacion("conv-2");
        dto.setUsuarioId("USR010");

        StepVerifier.create(chatbotService.procesarMensaje(dto))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertEquals(
                        "Te abrí el formulario de inicio de sesión.", resp.getRespuesta()))
                .verifyComplete();

        verify(herramientas).ejecutar(eq("iniciar_flujo_ui"), any(), eq("USR010"), any());
        // Se guarda: mensaje del usuario + resultado de la herramienta (tool) + respuesta final
        verify(repository, times(3)).save(any(MensajeChatbot.class));
    }

    @Test
    @DisplayName("iniciar_flujo_ui con tipo AGENDAR (paciente con cuenta) marca accionRealizada=REDIRIGIR_AGENDAR")
    void procesarMensaje_iniciarFlujoUiAgendar_marcaRedirigirAgendar() {
        when(repository.findByIdentificadorConversacionOrderByFechaHoraAsc("conv-3")).thenReturn(List.of());

        when(ollamaService.chat(anyList(), anyList()))
                .thenReturn(Mono.just(respuestaConToolCall("iniciar_flujo_ui", Map.of("tipo", "AGENDAR"))))
                .thenReturn(Mono.just(respuestaTexto("¡Claro! Te abrí el formulario para agendar tu cita.")));

        when(herramientas.ejecutar(eq("iniciar_flujo_ui"), any(), eq("USR010"), any()))
                .thenReturn(Mono.just("{\"exito\": true, \"tipoFormulario\": \"AGENDAR\"}"));

        MensajeRequestDTO dto = new MensajeRequestDTO();
        dto.setMensaje("Sí, quiero agendar una hora");
        dto.setIdentificadorConversacion("conv-3");
        dto.setUsuarioId("USR010");
        dto.setNombrePaciente("Juan Pérez");

        StepVerifier.create(chatbotService.procesarMensaje(dto))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertEquals("REDIRIGIR_AGENDAR", resp.getAccionRealizada()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Si una herramienta falla (exito=false), la siguiente respuesta se marca con emocion CONFUNDIDO")
    void procesarMensaje_herramientaFalla_emocionConfundido() {
        when(repository.findByIdentificadorConversacionOrderByFechaHoraAsc("conv-5")).thenReturn(List.of());

        when(ollamaService.chat(anyList(), anyList()))
                .thenReturn(Mono.just(respuestaConToolCall("iniciar_flujo_ui", Map.of("tipo", "ALGO_RARO"))))
                .thenReturn(Mono.just(respuestaTexto("Hubo un problema, ¿podrías repetir tu solicitud?")));

        when(herramientas.ejecutar(eq("iniciar_flujo_ui"), any(), eq("USR010"), any()))
                .thenReturn(Mono.just("{\"exito\": false, \"error\": \"Tipo de formulario no reconocido\"}"));

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
                .thenReturn(Mono.just(respuestaConToolCall("iniciar_flujo_ui", Map.of("tipo", "LOGIN"))));
        when(herramientas.ejecutar(any(), any(), any(), any()))
                .thenReturn(Mono.just("{\"exito\": true, \"tipoFormulario\": \"LOGIN\"}"));

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

    @Test
    @DisplayName("BUG REAL: si el modelo alucina un tool_call inventado como texto JSON crudo, se sanea en vez de mostrarlo al paciente")
    void procesarMensaje_modeloAlucinaToolCallComoTexto_sePreservaSanitizado() {
        // Reproduce exactamente el caso real detectado: el paciente pide
        // ayuda para registrarse y el modelo, en vez de usar tool_calls
        // estructurado o texto en espanol, devuelve un JSON inventado
        // como contenido de texto plano: {"name": "crear_cuenta", ...}
        when(repository.findByIdentificadorConversacionOrderByFechaHoraAsc("conv-8")).thenReturn(List.of());
        when(ollamaService.chat(anyList(), anyList())).thenReturn(Mono.just(
                respuestaTexto("{\"name\": \"crear_cuenta\", \"parameters\": {\"username\": \"\", \"password\": \"\"}}")));

        MensajeRequestDTO dto = new MensajeRequestDTO();
        dto.setMensaje("ayudame a registrarme");
        dto.setIdentificadorConversacion("conv-8");
        dto.setUsuarioId(null);

        StepVerifier.create(chatbotService.procesarMensaje(dto))
                .assertNext(resp -> {
                    org.junit.jupiter.api.Assertions.assertFalse(resp.getRespuesta().contains("\"name\""));
                    org.junit.jupiter.api.Assertions.assertFalse(resp.getRespuesta().contains("\"parameters\""));
                    org.junit.jupiter.api.Assertions.assertTrue(resp.getRespuesta().contains("¿Podrías"));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("iniciar_flujo_ui con tipo REGISTRO marca accionRealizada=REDIRIGIR_REGISTRO")
    void procesarMensaje_iniciarFlujoUiRegistro_marcaRedirigirRegistro() {
        when(repository.findByIdentificadorConversacionOrderByFechaHoraAsc("conv-9")).thenReturn(List.of());

        when(ollamaService.chat(anyList(), anyList()))
                .thenReturn(Mono.just(respuestaConToolCall("iniciar_flujo_ui", Map.of("tipo", "REGISTRO"))))
                .thenReturn(Mono.just(respuestaTexto("¡Claro! Te abrí el formulario de registro.")));

        when(herramientas.ejecutar(eq("iniciar_flujo_ui"), any(), isNull(), any()))
                .thenReturn(Mono.just("{\"exito\": true, \"tipoFormulario\": \"REGISTRO\"}"));

        MensajeRequestDTO dto = new MensajeRequestDTO();
        dto.setMensaje("ayudame a registrarme");
        dto.setIdentificadorConversacion("conv-9");
        dto.setUsuarioId(null);

        StepVerifier.create(chatbotService.procesarMensaje(dto))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertEquals("REDIRIGIR_REGISTRO", resp.getAccionRealizada()))
                .verifyComplete();
    }

    @Test
    @DisplayName("iniciar_flujo_ui con tipo LOGIN marca accionRealizada=REDIRIGIR_LOGIN")
    void procesarMensaje_iniciarFlujoUiLogin_marcaRedirigirLogin() {
        when(repository.findByIdentificadorConversacionOrderByFechaHoraAsc("conv-10")).thenReturn(List.of());

        when(ollamaService.chat(anyList(), anyList()))
                .thenReturn(Mono.just(respuestaConToolCall("iniciar_flujo_ui", Map.of("tipo", "LOGIN"))))
                .thenReturn(Mono.just(respuestaTexto("Te abrí el formulario de inicio de sesión.")));

        when(herramientas.ejecutar(eq("iniciar_flujo_ui"), any(), any(), any()))
                .thenReturn(Mono.just("{\"exito\": true, \"tipoFormulario\": \"LOGIN\"}"));

        MensajeRequestDTO dto = new MensajeRequestDTO();
        dto.setMensaje("no puedo entrar a mi cuenta");
        dto.setIdentificadorConversacion("conv-10");
        dto.setUsuarioId(null);

        StepVerifier.create(chatbotService.procesarMensaje(dto))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertEquals("REDIRIGIR_LOGIN", resp.getAccionRealizada()))
                .verifyComplete();
    }

    @Test
    @DisplayName("iniciar_flujo_ui con tipo RECUPERAR marca accionRealizada=REDIRIGIR_RECUPERAR")
    void procesarMensaje_iniciarFlujoUiRecuperar_marcaRedirigirRecuperar() {
        when(repository.findByIdentificadorConversacionOrderByFechaHoraAsc("conv-11")).thenReturn(List.of());

        when(ollamaService.chat(anyList(), anyList()))
                .thenReturn(Mono.just(respuestaConToolCall("iniciar_flujo_ui", Map.of("tipo", "RECUPERAR"))))
                .thenReturn(Mono.just(respuestaTexto("Te abrí el formulario para recuperar tu contraseña.")));

        when(herramientas.ejecutar(eq("iniciar_flujo_ui"), any(), any(), any()))
                .thenReturn(Mono.just("{\"exito\": true, \"tipoFormulario\": \"RECUPERAR\"}"));

        MensajeRequestDTO dto = new MensajeRequestDTO();
        dto.setMensaje("olvidé mi contraseña");
        dto.setIdentificadorConversacion("conv-11");
        dto.setUsuarioId(null);

        StepVerifier.create(chatbotService.procesarMensaje(dto))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertEquals("REDIRIGIR_RECUPERAR", resp.getAccionRealizada()))
                .verifyComplete();
    }

    @Test
    @DisplayName("BUG REAL: tras detectar RECUPERAR, se inyecta un recordatorio para que el modelo " +
            "no mezcle la palabra LOGIN en el texto de su respuesta")
    void procesarMensaje_iniciarFlujoUiRecuperar_inyectaRecordatorioAntiMezcla() {
        // Reproduce el caso real detectado: con llama3.2:3b, la ACCION
        // estructurada (tipoFormulario=RECUPERAR) ya salia correcta, pero
        // el TEXTO que el modelo redactaba a veces mencionaba "LOGIN" de
        // todas formas, lo cual confunde al paciente aunque el frontend
        // abra el modal correcto (decide por el campo, no por el texto).
        when(repository.findByIdentificadorConversacionOrderByFechaHoraAsc("conv-12")).thenReturn(List.of());

        when(ollamaService.chat(anyList(), anyList()))
                .thenReturn(Mono.just(respuestaConToolCall("iniciar_flujo_ui", Map.of("tipo", "RECUPERAR"))))
                .thenReturn(Mono.just(respuestaTexto("Te ayudo a recuperar tu contraseña.")));

        when(herramientas.ejecutar(eq("iniciar_flujo_ui"), any(), any(), any()))
                .thenReturn(Mono.just("{\"exito\": true, \"tipoFormulario\": \"RECUPERAR\"}"));

        MensajeRequestDTO dto = new MensajeRequestDTO();
        dto.setMensaje("olvidé mi contraseña");
        dto.setIdentificadorConversacion("conv-12");
        dto.setUsuarioId(null);

        StepVerifier.create(chatbotService.procesarMensaje(dto))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertEquals("REDIRIGIR_RECUPERAR", resp.getAccionRealizada()))
                .verifyComplete();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OllamaMensaje>> captor = ArgumentCaptor.forClass(List.class);
        verify(ollamaService, times(2)).chat(captor.capture(), anyList());

        List<OllamaMensaje> historialSegundaLlamada = captor.getAllValues().get(1);
        boolean tieneRecordatorio = historialSegundaLlamada.stream().anyMatch(m ->
                "system".equals(m.getRole())
                        && m.getContent().contains("RECORDATORIO")
                        && m.getContent().contains("RECUPERAR")
                        && m.getContent().contains("LOGIN"));

        org.junit.jupiter.api.Assertions.assertTrue(tieneRecordatorio,
                "Se esperaba un mensaje de sistema recordando no mezclar LOGIN en la respuesta de RECUPERAR");
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
