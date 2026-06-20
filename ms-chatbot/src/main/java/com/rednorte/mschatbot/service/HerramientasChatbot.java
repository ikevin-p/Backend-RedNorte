package com.rednorte.mschatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rednorte.mschatbot.dto.ollama.OllamaTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Implementacion REAL de las herramientas que SaludBot puede invocar.
 *
 * Cada metodo aqui pega de verdad a un microservicio existente
 * (ms-agenda-medica, ms-consultas) usando los mismos endpoints que ya
 * usa AgendarPage.jsx en el frontend. El resultado se devuelve como un
 * JSON (string) que se reinyecta en la conversacion con role="tool"
 * para que el modelo arme la respuesta final en lenguaje natural.
 */
@Service
public class HerramientasChatbot {

    private final WebClient agendaClient;
    private final WebClient consultasClient;
    private final ObjectMapper objectMapper;

    // Mismo catalogo de doctores/especialidades que usa el frontend
    // (AgendarPage.jsx), para que el bot ofrezca exactamente las mismas
    // especialidades reales que existen en el sistema.
    private static final Map<String, String> ESPECIALIDAD_POR_DOCTOR = Map.of(
            "USR002", "Cardiología",
            "USR003", "Medicina General",
            "USR004", "Traumatología"
    );

    public HerramientasChatbot(
            @Value("${rednorte.ms-agenda-medica.url}") String agendaUrl,
            @Value("${rednorte.ms-consultas.url}") String consultasUrl,
            ObjectMapper objectMapper) {
        this.agendaClient = WebClient.builder().baseUrl(agendaUrl).build();
        this.consultasClient = WebClient.builder().baseUrl(consultasUrl).build();
        this.objectMapper = objectMapper;
    }

    /** Las definiciones que se le envian a Ollama en cada turno. */
    public List<OllamaTool> definiciones() {
        return List.of(
                new OllamaTool(new OllamaTool.Funcion(
                        "buscar_horarios_disponibles",
                        "Busca horarios medicos disponibles para una fecha especifica. Usar SIEMPRE antes de prometer un horario al paciente.",
                        Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "fecha", Map.of("type", "string", "description", "Fecha en formato YYYY-MM-DD")
                                ),
                                "required", List.of("fecha")
                        )
                )),
                new OllamaTool(new OllamaTool.Funcion(
                        "crear_cita_real",
                        "Agenda definitivamente una cita medica REAL en el sistema. Solo usar despues de confirmar con el paciente la especialidad, fecha/hora exactas (de un bloque ya consultado con buscar_horarios_disponibles), nombre completo y RUT.",
                        Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "bloqueId", Map.of("type", "integer", "description", "ID del bloque horario elegido, obtenido de buscar_horarios_disponibles"),
                                        "nombrePaciente", Map.of("type", "string"),
                                        "rut", Map.of("type", "string"),
                                        "motivoConsulta", Map.of("type", "string", "description", "Breve descripcion de lo que el paciente busca tratar")
                                ),
                                "required", List.of("bloqueId", "nombrePaciente", "rut", "motivoConsulta")
                        )
                ))
        );
    }

    /** Despacha la herramienta solicitada por el modelo y devuelve el resultado como JSON string. */
    public Mono<String> ejecutar(String nombreHerramienta, Map<String, Object> argumentos,
                                  String usuarioId, String nombrePacienteSesion) {
        return switch (nombreHerramienta) {
            case "buscar_horarios_disponibles" -> buscarHorariosDisponibles((String) argumentos.get("fecha"));
            case "crear_cita_real" -> crearCitaReal(argumentos, usuarioId, nombrePacienteSesion);
            default -> Mono.just(error("Herramienta desconocida: " + nombreHerramienta));
        };
    }

    private Mono<String> buscarHorariosDisponibles(String fechaStr) {
        try {
            LocalDate fecha = LocalDate.parse(fechaStr, DateTimeFormatter.ISO_LOCAL_DATE);
            return agendaClient.get()
                    .uri("/agenda/disponibles/{fecha}", fecha)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(this::resumirBloquesParaElModelo)
                    .onErrorResume(e -> Mono.just(error("No se pudo consultar la agenda: " + e.getMessage())));
        } catch (Exception e) {
            return Mono.just(error("Formato de fecha invalido, debe ser YYYY-MM-DD."));
        }
    }

    /**
     * Reduce el JSON crudo de bloques a algo compacto y legible para el
     * modelo: solo id, hora y especialidad del doctor. Enviarle el JSON
     * completo (con establecimientoId, fechaCreacion, etc.) desperdicia
     * contexto y confunde al modelo sin aportar nada util a la conversacion.
     */
    private String resumirBloquesParaElModelo(JsonNode bloques) {
        var resumen = objectMapper.createArrayNode();
        for (JsonNode b : bloques) {
            String doctorId = b.path("doctorId").asText();
            var item = objectMapper.createObjectNode();
            item.put("bloqueId", b.path("id").asLong());
            item.put("hora", b.path("horaInicio").asText().substring(0, 5));
            item.put("especialidad", ESPECIALIDAD_POR_DOCTOR.getOrDefault(doctorId, "General"));
            resumen.add(item);
        }
        if (resumen.isEmpty()) {
            return "{\"disponible\": false, \"mensaje\": \"No hay horarios disponibles ese día.\"}";
        }
        return resumen.toString();
    }

    private Mono<String> crearCitaReal(Map<String, Object> argumentos, String usuarioId, String nombrePacienteSesion) {
        if (usuarioId == null) {
            // Visitante anonimo: no se puede agendar sin cuenta. El modelo
            // debe interpretar esto y guiar al registro (lo indicamos
            // explicitamente en el resultado para que no insista).
            return Mono.just("{\"exito\": false, \"requiereRegistro\": true, " +
                    "\"mensaje\": \"El paciente no tiene una cuenta. No se puede agendar sin registrarse primero.\"}");
        }

        Long bloqueId = ((Number) argumentos.get("bloqueId")).longValue();
        String nombrePaciente = nombrePacienteSesion != null ? nombrePacienteSesion : String.valueOf(argumentos.get("nombrePaciente"));
        String motivo = String.valueOf(argumentos.get("motivoConsulta"));

        // 1) Crear la consulta en ms-consultas
        Map<String, Object> nuevaConsulta = Map.of(
                "usuarioId", usuarioId,
                "nombrePaciente", nombrePaciente,
                "sintomas", motivo,
                "especialidad", "General",
                "estado", "AGENDADA"
        );

        return consultasClient.post()
                .uri("/consultas")
                .bodyValue(nuevaConsulta)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(consultaCreada -> {
                    Long consultaId = consultaCreada.path("id").asLong();
                    Map<String, Object> reserva = Map.of(
                            "pacienteId", usuarioId,
                            "consultaId", String.valueOf(consultaId)
                    );
                    return agendaClient.put()
                            .uri("/agenda/{id}/reservar", bloqueId)
                            .bodyValue(reserva)
                            .retrieve()
                            .bodyToMono(JsonNode.class)
                            .map(bloqueReservado -> {
                                var resultado = objectMapper.createObjectNode();
                                resultado.put("exito", true);
                                resultado.put("consultaId", consultaId);
                                resultado.put("fecha", bloqueReservado.path("fecha").asText());
                                resultado.put("hora", bloqueReservado.path("horaInicio").asText().substring(0, 5));
                                return resultado.toString();
                            });
                })
                .onErrorResume(e -> Mono.just(error(
                        "No se pudo agendar la cita, probablemente ese horario ya fue tomado por otro paciente. " +
                        "Sugiere al paciente elegir otro horario.")));
    }

    private String error(String mensaje) {
        return "{\"exito\": false, \"error\": \"" + mensaje.replace("\"", "'") + "\"}";
    }
}
