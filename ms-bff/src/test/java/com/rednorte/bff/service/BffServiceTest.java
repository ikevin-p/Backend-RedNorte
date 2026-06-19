package com.rednorte.bff.service;

import com.rednorte.bff.dto.DashboardDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios — BffService")
class BffServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BffService bffService;

    private static final String URL_USUARIOS = "http://cnt-ms-usuarios:8081";
    private static final String URL_CONSULTAS = "http://cnt-ms-consultas:8083";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(bffService, "urlUsuarios", URL_USUARIOS);
        ReflectionTestUtils.setField(bffService, "urlConsultas", URL_CONSULTAS);
    }

    @SuppressWarnings("unchecked")
    private void mockConsultas(List<Map> consultas) {
        when(restTemplate.exchange(
                eq(URL_CONSULTAS + "/consultas"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(consultas));
    }

    @SuppressWarnings("unchecked")
    private void mockUsuarios(List<Map> usuarios) {
        when(restTemplate.exchange(
                eq(URL_USUARIOS + "/usuarios"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(usuarios));
    }

    // ─── DASHBOARD: AGREGACION ───────────────────────────────────────────────

    @Test
    @DisplayName("Dashboard cuenta correctamente las consultas por estado")
    void obtenerDashboard_cuentaConsultasPorEstado() {
        List<Map> consultas = List.of(
                Map.of("estado", "PENDIENTE"),
                Map.of("estado", "PENDIENTE"),
                Map.of("estado", "AGENDADA"),
                Map.of("estado", "ATENDIDA"),
                Map.of("estado", "CANCELADA")
        );
        mockConsultas(consultas);
        mockUsuarios(List.of());

        DashboardDTO resultado = bffService.obtenerDashboard("Bearer token123");

        assertEquals(5, resultado.getTotalConsultas());
        assertEquals(2, resultado.getConsultasPendientes());
        assertEquals(1, resultado.getConsultasAgendadas());
        assertEquals(1, resultado.getConsultasAtendidas());
        assertEquals(1, resultado.getConsultasCanceladas());
    }

    @Test
    @DisplayName("Dashboard cuenta correctamente doctores y pacientes por rol")
    void obtenerDashboard_cuentaUsuariosPorRol() {
        List<Map> usuarios = List.of(
                Map.of("rol", Map.of("tag", "DOCTOR")),
                Map.of("rol", Map.of("tag", "DOCTOR")),
                Map.of("rol", Map.of("tag", "PACIENTE")),
                Map.of("rol", Map.of("tag", "ADMIN"))
        );
        mockConsultas(List.of());
        mockUsuarios(usuarios);

        DashboardDTO resultado = bffService.obtenerDashboard("Bearer token123");

        assertEquals(4, resultado.getTotalUsuarios());
        assertEquals(2, resultado.getTotalDoctores());
        assertEquals(1, resultado.getTotalPacientes());
    }

    // ─── PROPAGACION DE TOKEN JWT ────────────────────────────────────────────

    @Test
    @DisplayName("Dashboard reenvia el header Authorization a los microservicios internos")
    void obtenerDashboard_reenviaTokenAMicroserviciosInternos() {
        mockConsultas(List.of());
        mockUsuarios(List.of());
        String token = "Bearer eyJhbGciOiJIUzI1NiJ9.payload.signature";

        bffService.obtenerDashboard(token);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate, times(2)).exchange(
                anyString(), eq(HttpMethod.GET), captor.capture(), any(ParameterizedTypeReference.class));

        for (HttpEntity<?> entity : captor.getAllValues()) {
            assertEquals(token, entity.getHeaders().getFirst("Authorization"));
        }
    }

    @Test
    @DisplayName("Dashboard sin token no incluye header Authorization pero no falla")
    void obtenerDashboard_sinToken_noIncluyeHeaderPeroNoFalla() {
        mockConsultas(List.of());
        mockUsuarios(List.of());

        assertDoesNotThrow(() -> bffService.obtenerDashboard(null));

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate, atLeastOnce()).exchange(
                anyString(), eq(HttpMethod.GET), captor.capture(), any(ParameterizedTypeReference.class));
        assertNull(captor.getValue().getHeaders().getFirst("Authorization"));
    }

    // ─── MANEJO DE ERRORES ───────────────────────────────────────────────────

    @Test
    @DisplayName("Dashboard no lanza excepcion si ms-consultas falla (ej. 401 sin token valido)")
    void obtenerDashboard_msConsultasFalla_noLanzaExcepcion() {
        when(restTemplate.exchange(
                eq(URL_CONSULTAS + "/consultas"), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("401 Unauthorized"));
        mockUsuarios(List.of());

        DashboardDTO resultado = assertDoesNotThrow(() -> bffService.obtenerDashboard("Bearer token"));

        // Como fallo, el campo de consultas se queda en su valor por defecto (0)
        assertEquals(0, resultado.getTotalConsultas());
    }

    // ─── CONSULTAS DE UN PACIENTE ────────────────────────────────────────────

    @Test
    @DisplayName("Obtener consultas de paciente retorna la lista del microservicio")
    void obtenerConsultasPaciente_retornaListaCorrecta() {
        List<Map> consultas = List.of(Map.of("id", 1, "estado", "PENDIENTE"));
        when(restTemplate.exchange(
                eq(URL_CONSULTAS + "/consultas/usuario/USR001"),
                eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(consultas));

        List<Map> resultado = bffService.obtenerConsultasPaciente("USR001", "Bearer token");

        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("Obtener consultas de paciente retorna lista vacia si el microservicio falla")
    void obtenerConsultasPaciente_microservicioFalla_retornaListaVacia() {
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("Connection refused"));

        List<Map> resultado = bffService.obtenerConsultasPaciente("USR001", "Bearer token");

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}
