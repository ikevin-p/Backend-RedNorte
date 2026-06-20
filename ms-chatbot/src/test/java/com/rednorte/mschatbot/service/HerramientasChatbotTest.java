package com.rednorte.mschatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de HerramientasChatbot usando un servidor HTTP real embebido
 * (com.sun.net.httpserver, incluido en el JDK) en vez de mockear la
 * cadena fluida de WebClient con Mockito. Esto verifica el
 * comportamiento real de las llamadas HTTP (URI, body, parseo de
 * respuesta) sin depender de librerias adicionales de mocking HTTP
 * que el proyecto no tiene instaladas.
 */
@DisplayName("Tests unitarios — HerramientasChatbot")
class HerramientasChatbotTest {

    private HttpServer agendaServer;
    private HttpServer consultasServer;
    private HerramientasChatbot herramientas;

    @BeforeEach
    void setUp() throws Exception {
        agendaServer = HttpServer.create(new InetSocketAddress(0), 0);
        consultasServer = HttpServer.create(new InetSocketAddress(0), 0);
        agendaServer.start();
        consultasServer.start();

        String agendaUrl = "http://localhost:" + agendaServer.getAddress().getPort();
        String consultasUrl = "http://localhost:" + consultasServer.getAddress().getPort();

        herramientas = new HerramientasChatbot(agendaUrl, consultasUrl, new ObjectMapper());
    }

    @AfterEach
    void tearDown() {
        agendaServer.stop(0);
        consultasServer.stop(0);
    }

    private void responder(HttpServer server, String path, int status, String jsonBody) {
        server.createContext(path, exchange -> {
            byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
    }

    @Test
    @DisplayName("definiciones() retorna exactamente las 2 herramientas con sus nombres correctos")
    void definiciones_retornaLasDosHerramientas() {
        var tools = herramientas.definiciones();
        assertEquals(2, tools.size());
        assertEquals("buscar_horarios_disponibles", tools.get(0).getFunction().getName());
        assertEquals("crear_cita_real", tools.get(1).getFunction().getName());
    }

    @Test
    @DisplayName("buscar_horarios_disponibles con bloques reales los resume con especialidad correcta")
    void ejecutar_buscarHorarios_resumeBloquesConEspecialidad() {
        String bloquesJson = "[{\"id\":501,\"doctorId\":\"USR002\",\"horaInicio\":\"08:00:00\",\"estado\":\"DISPONIBLE\"}]";
        responder(agendaServer, "/agenda/disponibles/2026-06-22", 200, bloquesJson);

        String resultado = herramientas.ejecutar("buscar_horarios_disponibles", Map.of("fecha", "2026-06-22"), "USR010", "Juan Perez")
                .block();

        assertNotNull(resultado);
        assertTrue(resultado.contains("\"bloqueId\":501"));
        assertTrue(resultado.contains("08:00"));
        assertTrue(resultado.contains("Cardiología"));
    }

    @Test
    @DisplayName("buscar_horarios_disponibles sin bloques retorna mensaje de no disponibilidad")
    void ejecutar_buscarHorarios_sinBloques_retornaNoDisponible() {
        responder(agendaServer, "/agenda/disponibles/2026-06-23", 200, "[]");

        String resultado = herramientas.ejecutar("buscar_horarios_disponibles", Map.of("fecha", "2026-06-23"), "USR010", "Juan Perez")
                .block();

        assertNotNull(resultado);
        assertTrue(resultado.contains("\"disponible\": false"));
    }

    @Test
    @DisplayName("buscar_horarios_disponibles con fecha mal formada retorna error sin lanzar excepcion")
    void ejecutar_buscarHorarios_fechaInvalida_retornaError() {
        String resultado = herramientas.ejecutar("buscar_horarios_disponibles", Map.of("fecha", "no-es-una-fecha"), "USR010", "Juan Perez")
                .block();

        assertNotNull(resultado);
        assertTrue(resultado.contains("\"exito\": false"));
    }

    @Test
    @DisplayName("crear_cita_real sin usuarioId (visitante anonimo) retorna requiereRegistro=true")
    void ejecutar_crearCita_visitanteAnonimo_retornaRequiereRegistro() {
        String resultado = herramientas.ejecutar("crear_cita_real",
                        Map.of("bloqueId", 501, "nombrePaciente", "Juan", "rut", "11111111-1", "motivoConsulta", "Control"),
                        null, null)
                .block();

        assertNotNull(resultado);
        assertTrue(resultado.contains("\"requiereRegistro\": true"));
    }

    @Test
    @DisplayName("crear_cita_real exitoso crea la consulta, reserva el bloque y retorna exito=true")
    void ejecutar_crearCita_usuarioConSesion_agendaExitosamente() {
        responder(consultasServer, "/consultas", 200, "{\"id\":999}");
        responder(agendaServer, "/agenda/501/reservar", 200,
                "{\"fecha\":\"2026-06-22\",\"horaInicio\":\"08:00:00\",\"estado\":\"RESERVADO\"}");

        String resultado = herramientas.ejecutar("crear_cita_real",
                        Map.of("bloqueId", 501, "nombrePaciente", "Juan Perez", "rut", "11111111-1", "motivoConsulta", "Control"),
                        "USR010", "Juan Perez")
                .block();

        assertNotNull(resultado);
        assertTrue(resultado.contains("\"exito\":true"));
        assertTrue(resultado.contains("\"consultaId\":999"));
        assertTrue(resultado.contains("08:00"));
    }

    @Test
    @DisplayName("crear_cita_real cuando el bloque ya fue tomado (reserva falla) retorna error claro")
    void ejecutar_crearCita_bloqueYaTomado_retornaError() {
        responder(consultasServer, "/consultas", 200, "{\"id\":999}");
        agendaServer.createContext("/agenda/501/reservar", exchange -> {
            exchange.sendResponseHeaders(400, -1);
            exchange.close();
        });

        String resultado = herramientas.ejecutar("crear_cita_real",
                        Map.of("bloqueId", 501, "nombrePaciente", "Juan Perez", "rut", "11111111-1", "motivoConsulta", "Control"),
                        "USR010", "Juan Perez")
                .block();

        assertNotNull(resultado);
        assertTrue(resultado.contains("\"exito\": false"));
        assertTrue(resultado.contains("ya fue tomado"));
    }

    @Test
    @DisplayName("ejecutar con un nombre de herramienta desconocido retorna error sin lanzar excepcion")
    void ejecutar_herramientaDesconocida_retornaError() {
        String resultado = herramientas.ejecutar("herramienta_inexistente", Map.of(), "USR010", "Juan Perez")
                .block();

        assertNotNull(resultado);
        assertTrue(resultado.contains("Herramienta desconocida"));
    }
}
