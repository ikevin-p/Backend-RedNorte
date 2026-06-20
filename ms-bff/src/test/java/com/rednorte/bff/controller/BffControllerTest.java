package com.rednorte.bff.controller;

import com.rednorte.bff.dto.DashboardDTO;
import com.rednorte.bff.security.JwtUtilCompartido;
import com.rednorte.bff.service.BffService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de la capa HTTP de BffController.
 */
@WebMvcTest(BffController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests HTTP — BffController")
class BffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BffService bffService;

    @MockBean
    private JwtUtilCompartido jwtUtilCompartido;

    @Test
    @DisplayName("GET /bff/dashboard retorna 200 con los totales consolidados")
    void dashboard_conToken_retorna200ConTotales() throws Exception {
        DashboardDTO dto = new DashboardDTO();
        dto.setTotalConsultas(20);
        dto.setTotalUsuarios(8);

        when(bffService.obtenerDashboard("Bearer token-valido")).thenReturn(dto);

        mockMvc.perform(get("/bff/dashboard").header("Authorization", "Bearer token-valido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalConsultas").value(20))
                .andExpect(jsonPath("$.totalUsuarios").value(8));
    }

    @Test
    @DisplayName("GET /bff/dashboard sin header Authorization tambien retorna 200")
    void dashboard_sinToken_retorna200() throws Exception {
        when(bffService.obtenerDashboard(null)).thenReturn(new DashboardDTO());

        mockMvc.perform(get("/bff/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /bff/paciente/{id}/consultas retorna las consultas del paciente")
    void consultasPaciente_retorna200ConLista() throws Exception {
        when(bffService.obtenerConsultasPaciente(eq("USR010"), any()))
                .thenReturn(List.of(Map.of("id", 1, "estado", "PENDIENTE")));

        mockMvc.perform(get("/bff/paciente/USR010/consultas").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    @DisplayName("GET /bff/health responde 200 sin requerir autenticacion")
    void health_retorna200ConStatusUp() throws Exception {
        mockMvc.perform(get("/bff/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.servicio").value("ms-bff"));
    }
}
