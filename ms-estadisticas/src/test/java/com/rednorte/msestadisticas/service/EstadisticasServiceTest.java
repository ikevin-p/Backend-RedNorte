package com.rednorte.msestadisticas.service;

import com.rednorte.msestadisticas.dto.EstadisticasDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de EstadisticasService.
 *
 * EstadisticasService usa WebClient.Builder con una cadena fluida
 * (build().get().uri(...).header(...).retrieve().bodyToMono(...).block()),
 * por lo que es necesario mockear cada eslabon de esa cadena.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios — EstadisticasService")
class EstadisticasServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    private EstadisticasService service;

    private static final String URL_CONSULTAS = "http://cnt-ms-consultas:8083";
    private static final String URL_USUARIOS = "http://cnt-ms-usuarios:8081";
    private static final String URL_AGENDA = "http://cnt-ms-agenda-medica:8094";
    private static final String URL_ESTABLECIMIENTOS = "http://cnt-ms-establecimientos:8093";

    @BeforeEach
    void setUp() {
        service = new EstadisticasService(webClientBuilder);
        ReflectionTestUtils.setField(service, "consultasUrl", URL_CONSULTAS);
        ReflectionTestUtils.setField(service, "usuariosUrl", URL_USUARIOS);
        ReflectionTestUtils.setField(service, "agendaUrl", URL_AGENDA);
        ReflectionTestUtils.setField(service, "establecimientosUrl", URL_ESTABLECIMIENTOS);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @SuppressWarnings("unchecked")
    private void mockRespuesta(List<Map<String, Object>> datos) {
        when(responseSpec.bodyToMono(List.class)).thenReturn(Mono.just(datos));
    }

    // ─── RESUMEN GENERAL ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Resumen cuenta correctamente consultas por estado y por doctor")
    void obtenerResumen_cuentaConsultasPorEstadoYDoctor() {
        List<Map<String, Object>> consultas = List.of(
                Map.of("estado", "PENDIENTE", "doctorId", "DOC001"),
                Map.of("estado", "PENDIENTE", "doctorId", "DOC001"),
                Map.of("estado", "ATENDIDA", "doctorId", "DOC002")
        );
        mockRespuesta(consultas);

        EstadisticasDTO resultado = service.obtenerResumen("Bearer token123");

        assertEquals(3, resultado.getTotalConsultas());
        assertEquals(2L, resultado.getConsultasPorEstado().get("PENDIENTE"));
        assertEquals(1L, resultado.getConsultasPorEstado().get("ATENDIDA"));
        assertEquals(2L, resultado.getConsultasPorDoctor().get("DOC001"));
    }

    @Test
    @DisplayName("Resumen propaga el token JWT en cada llamada a otros microservicios")
    void obtenerResumen_propagaTokenEnCadaLlamada() {
        mockRespuesta(List.of());
        String token = "Bearer eyJhbGciOiJIUzI1NiJ9.payload.signature";

        service.obtenerResumen(token);

        // Se hacen 4 llamadas internas (consultas, usuarios, agenda, establecimientos)
        verify(requestHeadersSpec, times(4)).header("Authorization", token);
    }

    @Test
    @DisplayName("Resumen sin token no agrega el header pero no falla")
    void obtenerResumen_sinToken_noAgregaHeaderNiFalla() {
        mockRespuesta(List.of());

        assertDoesNotThrow(() -> service.obtenerResumen(null));

        verify(requestHeadersSpec, never()).header(eq("Authorization"), anyString());
    }

    @Test
    @DisplayName("Resumen marca -1 en un total cuando el microservicio correspondiente falla")
    void obtenerResumen_microservicioFalla_marcaMenosUno() {
        when(responseSpec.bodyToMono(List.class))
                .thenReturn(Mono.error(new RuntimeException("Connection refused")));

        EstadisticasDTO resultado = service.obtenerResumen("Bearer token");

        assertEquals(-1, resultado.getTotalConsultas());
        assertEquals(-1, resultado.getTotalUsuarios());
        assertEquals(-1, resultado.getTotalBloquesAgenda());
        assertEquals(-1, resultado.getTotalEstablecimientos());
    }

    // ─── ESTADISTICAS DE CONSULTAS ───────────────────────────────────────────

    @Test
    @DisplayName("Estadisticas de consultas retorna total y distribucion por estado")
    void estadisticasConsultas_retornaTotalYDistribucion() {
        List<Map<String, Object>> consultas = List.of(
                Map.of("estado", "AGENDADA", "doctorId", "DOC001"),
                Map.of("estado", "CANCELADA", "doctorId", "SIN_ASIGNAR")
        );
        mockRespuesta(consultas);

        Map<String, Object> resultado = service.estadisticasConsultas("Bearer token");

        assertEquals(2, resultado.get("total"));
        assertNotNull(resultado.get("porEstado"));
        assertNotNull(resultado.get("porDoctor"));
    }

    @Test
    @DisplayName("Estadisticas de consultas retorna mensaje de error si falla la conexion")
    void estadisticasConsultas_fallaConexion_retornaError() {
        when(responseSpec.bodyToMono(List.class))
                .thenReturn(Mono.error(new RuntimeException("timeout")));

        Map<String, Object> resultado = service.estadisticasConsultas("Bearer token");

        assertEquals("No se pudo conectar con ms-consultas", resultado.get("error"));
    }

    // ─── ESTADISTICAS DE AGENDA ──────────────────────────────────────────────

    @Test
    @DisplayName("Estadisticas de agenda retorna total y distribucion por estado y doctor")
    void estadisticasAgenda_retornaTotalYDistribucion() {
        List<Map<String, Object>> bloques = List.of(
                Map.of("estado", "DISPONIBLE", "doctorId", "DOC001"),
                Map.of("estado", "OCUPADO", "doctorId", "DOC002"),
                Map.of("estado", "OCUPADO", "doctorId", "DOC002")
        );
        mockRespuesta(bloques);

        Map<String, Object> resultado = service.estadisticasAgenda("Bearer token");

        assertEquals(3, resultado.get("total"));
        @SuppressWarnings("unchecked")
        Map<String, Long> porDoctor = (Map<String, Long>) resultado.get("porDoctor");
        assertEquals(2L, porDoctor.get("DOC002"));
    }

    @Test
    @DisplayName("Estadisticas de agenda retorna mensaje de error si falla la conexion")
    void estadisticasAgenda_fallaConexion_retornaError() {
        when(responseSpec.bodyToMono(List.class))
                .thenReturn(Mono.error(new RuntimeException("timeout")));

        Map<String, Object> resultado = service.estadisticasAgenda("Bearer token");

        assertEquals("No se pudo conectar con ms-agenda-medica", resultado.get("error"));
    }
}
