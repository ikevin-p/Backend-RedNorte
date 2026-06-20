package com.rednorte.Backend_reasignacion.controller;

import com.rednorte.Backend_reasignacion.model.Cancelacion;
import com.rednorte.Backend_reasignacion.security.JwtUtilCompartido;
import com.rednorte.Backend_reasignacion.service.ReasignacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de la capa HTTP de ReasignacionController.
 */
@WebMvcTest(ReasignacionController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests HTTP — ReasignacionController")
class ReasignacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReasignacionService reasignacionService;

    @MockBean
    private JwtUtilCompartido jwtUtilCompartido;

    private Cancelacion cancelacionEjemplo;

    @BeforeEach
    void setUp() {
        cancelacionEjemplo = new Cancelacion(null, LocalDateTime.now(), 1L, "Paciente no puede asistir");
    }

    @Test
    @DisplayName("POST /api/reasignacion/solo-cancelar/{id} cancela sin reasignar y retorna 200")
    void cancelarCita_idValido_retorna200() throws Exception {
        when(reasignacionService.procesarSoloCancelacion(1L, "Paciente no puede asistir"))
                .thenReturn(cancelacionEjemplo);

        mockMvc.perform(post("/api/reasignacion/solo-cancelar/1")
                        .param("motivo", "Paciente no puede asistir"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motivo").value("Paciente no puede asistir"));
    }

    @Test
    @DisplayName("POST /api/reasignacion/cancelar-y-reasignar/{id} ejecuta ambos pasos y retorna 200")
    void procesoCompleto_exitoso_retorna200() throws Exception {
        when(reasignacionService.procesarSoloCancelacion(1L, "Cancelacion administrativa"))
                .thenReturn(cancelacionEjemplo);
        doNothing().when(reasignacionService).ejecutarReasignacion(any(Cancelacion.class), anyString());

        mockMvc.perform(post("/api/reasignacion/cancelar-y-reasignar/1")
                        .param("motivo", "Cancelacion administrativa")
                        .header("Authorization", "Bearer token-admin"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("exitosamente")));
    }

    @Test
    @DisplayName("POST /api/reasignacion/cancelar-y-reasignar/{id} cuando falla retorna 500 con el mensaje")
    void procesoCompleto_serviceLanzaExcepcion_retorna500() throws Exception {
        when(reasignacionService.procesarSoloCancelacion(anyLong(), anyString()))
                .thenThrow(new RuntimeException("No hay pacientes pendientes para reasignar"));

        mockMvc.perform(post("/api/reasignacion/cancelar-y-reasignar/1")
                        .param("motivo", "Cancelacion administrativa"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No hay pacientes pendientes")));
    }
}
