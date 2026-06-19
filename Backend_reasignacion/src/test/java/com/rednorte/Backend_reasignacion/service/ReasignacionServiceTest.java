package com.rednorte.Backend_reasignacion.service;

import com.rednorte.Backend_reasignacion.model.BloquesAgenda;
import com.rednorte.Backend_reasignacion.model.Cancelacion;
import com.rednorte.Backend_reasignacion.repository.BloquesAgendaRepository;
import com.rednorte.Backend_reasignacion.repository.CancelacionRepository;
import com.rednorte.Backend_reasignacion.repository.ReasignacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios — ReasignacionService")
class ReasignacionServiceTest {

    @Mock
    private BloquesAgendaRepository bloquesRepository;
    @Mock
    private CancelacionRepository cancelacionRepository;
    @Mock
    private ReasignacionRepository reasignacionRepository;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ReasignacionService service;

    private BloquesAgenda bloqueMock;

    @BeforeEach
    void setUp() {
        bloqueMock = new BloquesAgenda("cardiologia", LocalDateTime.now().plusDays(1), 10L, "DOC001");
    }

    // ─── PROCESAR SOLO CANCELACION ───────────────────────────────────────────

    @Test
    @DisplayName("Procesar solo cancelacion guarda la cancelacion con el bloque correcto")
    void procesarSoloCancelacion_bloqueExistente_guardaCancelacion() {
        when(bloquesRepository.findById(10L)).thenReturn(Optional.of(bloqueMock));
        when(cancelacionRepository.save(any(Cancelacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Cancelacion resultado = service.procesarSoloCancelacion(10L, "Paciente no puede asistir");

        assertNotNull(resultado);
        assertEquals(bloqueMock, resultado.getBloque());
        assertEquals("Paciente no puede asistir", resultado.getMotivo());
        verify(cancelacionRepository, times(1)).save(any(Cancelacion.class));
    }

    @Test
    @DisplayName("Procesar solo cancelacion con bloque inexistente lanza excepcion")
    void procesarSoloCancelacion_bloqueInexistente_lanzaExcepcion() {
        when(bloquesRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.procesarSoloCancelacion(999L, "motivo"));

        assertTrue(ex.getMessage().contains("999"));
        verify(cancelacionRepository, never()).save(any());
    }

    // ─── EJECUTAR REASIGNACION ───────────────────────────────────────────────

    @Test
    @DisplayName("Ejecutar reasignacion exitosa marca el log con exito=true y propaga el token JWT")
    void ejecutarReasignacion_consultaEncontrada_marcaExitoYPropagaToken() {
        Cancelacion cancelacion = new Cancelacion(bloqueMock, LocalDateTime.now(), 1L, "motivo");
        String tokenSimulado = "Bearer eyJhbGciOiJIUzI1NiJ9.fake.token";

        when(restTemplate.exchange(
                contains("/consultas/prioritario/cardiologia"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Long.class)
        )).thenReturn(ResponseEntity.ok(123L));

        service.ejecutarReasignacion(cancelacion, tokenSimulado);

        // Verifica que el header Authorization fue efectivamente reenviado
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), entityCaptor.capture(), eq(Long.class));
        assertEquals(tokenSimulado, entityCaptor.getValue().getHeaders().getFirst("Authorization"));

        assertTrue(cancelacion.getProcesado());
        verify(reasignacionRepository, times(1)).save(argThat(log -> Boolean.TRUE.equals(log.getExito())
                && log.getPacienteIdNuevo().equals(123L)));
    }

    @Test
    @DisplayName("Ejecutar reasignacion sin pacientes disponibles marca el log con exito=false")
    void ejecutarReasignacion_sinPacientesDisponibles_marcaFallo() {
        Cancelacion cancelacion = new Cancelacion(bloqueMock, LocalDateTime.now(), 2L, "motivo");

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Long.class)
        )).thenThrow(new RuntimeException("404 Not Found: no hay consultas pendientes"));

        service.ejecutarReasignacion(cancelacion, "Bearer cualquier-token");

        assertTrue(cancelacion.getProcesado());
        verify(reasignacionRepository, times(1)).save(argThat(log -> Boolean.FALSE.equals(log.getExito())
                && log.getPacienteIdNuevo() == null));
    }

    @Test
    @DisplayName("Ejecutar reasignacion sin token no rompe la llamada (header simplemente no se envia)")
    void ejecutarReasignacion_sinToken_noLanzaExcepcion() {
        Cancelacion cancelacion = new Cancelacion(bloqueMock, LocalDateTime.now(), 3L, "motivo");

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Long.class)
        )).thenReturn(ResponseEntity.ok(456L));

        assertDoesNotThrow(() -> service.ejecutarReasignacion(cancelacion, null));

        verify(reasignacionRepository, times(1)).save(any());
    }
}
