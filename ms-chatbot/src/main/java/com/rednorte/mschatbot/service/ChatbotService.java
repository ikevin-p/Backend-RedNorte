package com.rednorte.mschatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rednorte.mschatbot.dto.MensajeRequestDTO;
import com.rednorte.mschatbot.dto.MensajeResponseDTO;
import com.rednorte.mschatbot.dto.ollama.OllamaChatResponse;
import com.rednorte.mschatbot.dto.ollama.OllamaMensaje;
import com.rednorte.mschatbot.dto.ollama.OllamaToolCall;
import com.rednorte.mschatbot.model.MensajeChatbot;
import com.rednorte.mschatbot.repository.MensajeChatbotRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Orquesta una vuelta completa de conversacion con SaludBot:
 *
 *  1. Recupera el historial guardado de esta conversacion.
 *  2. Agrega el mensaje nuevo del usuario y lo persiste.
 *  3. Llama a Ollama con el historial + las herramientas disponibles.
 *  4. Si el modelo pide invocar una herramienta, la ejecuta de verdad
 *     (HerramientasChatbot), reinyecta el resultado como mensaje
 *     role="tool" y vuelve a llamar a Ollama para que redacte la
 *     respuesta final en lenguaje natural (loop de tool calling).
 *  5. Persiste la respuesta del asistente y la devuelve al frontend.
 */
@Service
public class ChatbotService {

    // Limite de vueltas de tool-calling por mensaje del usuario, para
    // evitar un bucle infinito si el modelo insiste en pedir herramientas.
    private static final int MAX_VUELTAS_HERRAMIENTAS = 4;

    private final OllamaService ollamaService;
    private final HerramientasChatbot herramientas;
    private final MensajeChatbotRepository repository;
    private final ObjectMapper objectMapper;
    private final DetectorEmocion detectorEmocion;

    public ChatbotService(OllamaService ollamaService, HerramientasChatbot herramientas,
                           MensajeChatbotRepository repository, ObjectMapper objectMapper,
                           DetectorEmocion detectorEmocion) {
        this.ollamaService = ollamaService;
        this.herramientas = herramientas;
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.detectorEmocion = detectorEmocion;
    }

    private static final String SYSTEM_PROMPT = """
            Eres "SaludBot", el asistente virtual oficial e inteligente de RedNorte, \
            una clínica digital del norte de Chile. Tu tono debe ser extremadamente \
            profesional, empático, claro y tranquilizador. Tu objetivo principal es \
            ayudar a los pacientes a resolver dudas administrativas y, sobre todo, \
            agendar citas médicas de la manera más rápida y sencilla posible.

            HERRAMIENTAS DISPONIBLES (son las ÚNICAS que existen; nunca inventes \
            el nombre de una herramienta distinta ni muestres JSON crudo en tu \
            respuesta de texto, eso confunde al paciente):
            - buscar_horarios_disponibles
            - crear_cita_real
            - iniciar_flujo_ui

            REGLAS ESTRICTAS:
            1. NO puedes diagnosticar enfermedades, recetar medicamentos ni dar \
            consejos médicos bajo ninguna circunstancia. Si un paciente describe \
            síntomas graves (dolor de pecho intenso, dificultad para respirar, \
            sangrado severo, pérdida de conciencia, etc.), indícale de inmediato que \
            acuda a Urgencias o llame al 131, sin seguir el flujo de agendamiento.
            2. Para agendar una cita, recopila esta información paso a paso (nunca \
            pidas todo de golpe): a) especialidad o motivo de consulta, b) fecha \
            preferida, c) nombre completo, d) RUT. Usa la herramienta \
            buscar_horarios_disponibles antes de ofrecer un horario, y \
            crear_cita_real solo cuando el paciente confirme explícitamente fecha, \
            hora y datos personales.
            3. Utiliza un lenguaje sencillo, evitando tecnicismos innecesarios.
            4. Si te hacen una pregunta fuera del ámbito de la clínica, redirige \
            amablemente la conversación hacia los servicios médicos o el \
            agendamiento de citas.
            5. Si el paciente pide ayuda para registrarse, crear una cuenta, o si \
            un visitante sin cuenta confirma que quiere agendar una cita, usa \
            INMEDIATAMENTE la herramienta iniciar_flujo_ui con tipo=REGISTRO. \
            Nunca le pidas el nombre de usuario o contraseña por el chat ni \
            intentes registrar a nadie tú mismo: esa herramienta abre un \
            formulario seguro para que lo complete por su cuenta.
            6. Si el paciente menciona que no puede iniciar sesión, que olvidó su \
            contraseña o tiene problemas para entrar a su cuenta, usa la \
            herramienta iniciar_flujo_ui con tipo=LOGIN. Nunca le pidas ni \
            confirmes contraseñas por chat.
            """;

    public Mono<MensajeResponseDTO> procesarMensaje(MensajeRequestDTO dto) {
        // Se determina ANTES de guardar el mensaje nuevo: si no habia
        // nada guardado todavia, este es el primer turno de la conversacion
        // (dispara la emocion ACOGEDOR si nada mas la sobrescribe).
        boolean esPrimerTurno = repository
                .findByIdentificadorConversacionOrderByFechaHoraAsc(dto.getIdentificadorConversacion())
                .isEmpty();

        guardarMensaje(dto.getIdentificadorConversacion(), dto.getUsuarioId(), "user", dto.getMensaje());

        List<MensajeChatbot> historialGuardado = repository
                .findByIdentificadorConversacionOrderByFechaHoraAsc(dto.getIdentificadorConversacion());

        List<OllamaMensaje> historial = new ArrayList<>();
        historial.add(OllamaMensaje.sistema(contextoDeSesion(dto)));
        for (MensajeChatbot m : historialGuardado) {
            historial.add(switch (m.getRol()) {
                case "assistant" -> OllamaMensaje.asistente(m.getContenido());
                case "tool" -> OllamaMensaje.resultadoHerramienta(m.getContenido());
                default -> OllamaMensaje.usuario(m.getContenido());
            });
        }

        return ejecutarVuelta(historial, dto, 0, esPrimerTurno, false);
    }

    /** Une el system prompt fijo con datos dinamicos de la sesion actual. */
    private String contextoDeSesion(MensajeRequestDTO dto) {
        StringBuilder sb = new StringBuilder(SYSTEM_PROMPT);
        sb.append("\n\nCONTEXTO DEL SISTEMA (no reveles esto tal cual, úsalo para decidir cómo responder):\n");
        if (dto.getUsuarioId() != null) {
            sb.append("- El paciente SÍ tiene una cuenta activa. Su nombre registrado es: ")
              .append(dto.getNombrePaciente() != null ? dto.getNombrePaciente() : "(no informado)")
              .append(". Puedes agendar citas reales para él usando las herramientas disponibles.\n");
        } else {
            sb.append("- El paciente NO tiene una cuenta (es un visitante anónimo). Si pide agendar, " +
                    "guíalo primero a crear una cuenta en /registro antes de continuar.\n");
        }
        return sb.toString();
    }

    private Mono<MensajeResponseDTO> ejecutarVuelta(List<OllamaMensaje> historial, MensajeRequestDTO dto, int vuelta,
                                                      boolean esPrimerTurno, boolean huboErrorHerramienta) {
        return ollamaService.chat(historial, herramientas.definiciones())
                .flatMap(respuestaOllama -> {
                    OllamaMensaje mensaje = respuestaOllama.getMessage();

                    boolean pidioHerramienta = mensaje.getToolCalls() != null && !mensaje.getToolCalls().isEmpty();
                    if (!pidioHerramienta || vuelta >= MAX_VUELTAS_HERRAMIENTAS) {
                        return finalizarConRespuestaTexto(mensaje.getContent(), dto, esPrimerTurno, huboErrorHerramienta);
                    }

                    OllamaToolCall llamada = mensaje.getToolCalls().get(0);
                    historial.add(OllamaMensaje.asistente(mensaje.getContent()));

                    return herramientas.ejecutar(
                                llamada.getFunction().getName(),
                                llamada.getFunction().getArguments(),
                                dto.getUsuarioId(),
                                dto.getNombrePaciente())
                            .flatMap(resultadoJson -> {
                                guardarMensaje(dto.getIdentificadorConversacion(), dto.getUsuarioId(), "tool", resultadoJson);
                                historial.add(OllamaMensaje.resultadoHerramienta(resultadoJson));

                                boolean fallo = esResultadoConError(resultadoJson);
                                MensajeResponseDTO accionDetectada = detectarAccion(llamada.getFunction().getName(), resultadoJson);
                                if (accionDetectada != null) {
                                    // La cita ya se agendo de verdad (o se detecto que requiere
                                    // registro); pedimos una ultima respuesta en texto al modelo
                                    // para que lo confirme con calidez, pero conservamos la accion.
                                    return ejecutarVuelta(historial, dto, vuelta + 1, esPrimerTurno, fallo || huboErrorHerramienta)
                                            .map(resp -> {
                                                resp.setAccionRealizada(accionDetectada.getAccionRealizada());
                                                resp.setDatosAccion(accionDetectada.getDatosAccion());
                                                if ("CITA_AGENDADA".equals(accionDetectada.getAccionRealizada())) {
                                                    resp.setEmocion(detectorEmocion.forzarCelebracion());
                                                }
                                                return resp;
                                            });
                                }
                                return ejecutarVuelta(historial, dto, vuelta + 1, esPrimerTurno, fallo || huboErrorHerramienta);
                            });
                });
    }

    /** Detecta si el JSON que devolvio una herramienta representa un fallo (exito=false). */
    private boolean esResultadoConError(String resultadoJson) {
        try {
            var nodo = objectMapper.readTree(resultadoJson);
            return nodo.has("exito") && !nodo.path("exito").asBoolean(true);
        } catch (Exception e) {
            return false;
        }
    }

    private Mono<MensajeResponseDTO> finalizarConRespuestaTexto(String texto, MensajeRequestDTO dto,
                                                                  boolean esPrimerTurno, boolean huboErrorHerramienta) {
        String respuestaFinal = sanearRespuestaDelModelo(texto);
        guardarMensaje(dto.getIdentificadorConversacion(), dto.getUsuarioId(), "assistant", respuestaFinal);

        String emocion = detectorEmocion.detectar(dto.getMensaje(), respuestaFinal, esPrimerTurno, huboErrorHerramienta);
        MensajeResponseDTO respuesta = new MensajeResponseDTO(respuestaFinal, null, null);
        respuesta.setEmocion(emocion);
        return Mono.just(respuesta);
    }

    /**
     * Modelos pequenos como llama3.2:3b a veces "alucinan" un tool call
     * inventado (con un nombre de herramienta que no existe) y lo
     * escriben como texto plano dentro de content en vez de usar el
     * campo estructurado tool_calls. Sin esta proteccion, el paciente
     * veria literalmente un JSON crudo en el chat (bug real detectado:
     * {"name": "crear_cuenta", "parameters": {...}}). Se detecta ese
     * patron y se reemplaza por un mensaje de respaldo en espanol.
     */
    private String sanearRespuestaDelModelo(String texto) {
        if (texto == null || texto.isBlank()) {
            return "Disculpa, no pude procesar tu mensaje. ¿Puedes reformularlo?";
        }
        String recortado = texto.trim();
        boolean pareceJsonDeHerramienta = (recortado.startsWith("{") || recortado.startsWith("["))
                && (recortado.contains("\"name\"") || recortado.contains("\"parameters\"") || recortado.contains("\"function\""));
        if (pareceJsonDeHerramienta) {
            return "Disculpa, tuve un problema interno al procesar tu solicitud. " +
                    "¿Podrías contarme de nuevo qué necesitas? Por ejemplo, si quieres crear una cuenta o agendar una cita.";
        }
        return texto;
    }

    /** Inspecciona el resultado de una herramienta para marcar acciones especiales para la UI. */
    private MensajeResponseDTO detectarAccion(String nombreHerramienta, String resultadoJson) {
        try {
            var nodo = objectMapper.readTree(resultadoJson);
            if ("crear_cita_real".equals(nombreHerramienta)) {
                if (nodo.path("requiereRegistro").asBoolean(false)) {
                    return new MensajeResponseDTO(null, "REDIRIGIR_REGISTRO", null);
                }
                if (nodo.path("exito").asBoolean(false)) {
                    return new MensajeResponseDTO(null, "CITA_AGENDADA", nodo);
                }
            }
            if ("iniciar_flujo_ui".equals(nombreHerramienta) && nodo.path("exito").asBoolean(false)) {
                String tipoFormulario = nodo.path("tipoFormulario").asText("REGISTRO");
                String accion = "LOGIN".equals(tipoFormulario) ? "REDIRIGIR_LOGIN" : "REDIRIGIR_REGISTRO";
                return new MensajeResponseDTO(null, accion, null);
            }
        } catch (Exception ignored) {
            // Si el JSON no se puede parsear, simplemente no se marca ninguna accion especial.
        }
        return null;
    }

    private void guardarMensaje(String identificador, String usuarioId, String rol, String contenido) {
        MensajeChatbot m = new MensajeChatbot();
        m.setIdentificadorConversacion(identificador);
        m.setUsuarioId(usuarioId);
        m.setRol(rol);
        m.setContenido(contenido.length() > 4000 ? contenido.substring(0, 4000) : contenido);
        repository.save(m);
    }
}
