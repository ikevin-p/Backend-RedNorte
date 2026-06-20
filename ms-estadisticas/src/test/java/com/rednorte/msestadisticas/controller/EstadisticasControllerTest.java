package com.rednorte.msestadisticas.controller;

import com.rednorte.msestadisticas.dto.EstadisticasDTO;
import com.rednorte.msestadisticas.security.JwtUtilCompartido;
import com.rednorte.msestadisticas.service.EstadisticasService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de la capa HTTP de EstadisticasController.
 *
 * @WebMvcTest carga solo el contexto web; se desactivan los filtros de
 * seguridad (addFilters = false) porque la autenticacion ya esta cubierta
 * por JwtAuthFilterTest. JwtUtilCompartido se mockea para que Spring pueda
 * construir el bean JwtAuthFilter (escaneado por ser @Component).
 */
@WebMvcTest(EstadisticasController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests HTTP — EstadisticasController")
class EstadisticasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EstadisticasService estadisticasService;

    @MockBean
    private JwtUtilCompartido jwtUtilCompartido;

    @Test
    @DisplayName("GET /estadisticas/resumen retorna 200 con los totales del sistema")
    void resumen_conToken_retorna200ConTotales() throws Exception {
        EstadisticasDTO dto = new EstadisticasDTO();
        dto.setTotalConsultas(42);
        dto.setTotalUsuarios(15);
        dto.setTotalBloquesAgenda(120);
        dto.setTotalEstablecimientos(5);

        when(estadisticasService.obtenerResumen("Bearer token-valido")).thenReturn(dto);

        mockMvc.perform(get("/estadisticas/resumen").header("Authorization", "Bearer token-valido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalConsultas").value(42))
                .andExpect(jsonPath("$.totalEstablecimientos").value(5));
    }

    @Test
    @DisplayName("GET /estadisticas/resumen sin header Authorization tambien retorna 200")
    void resumen_sinToken_retorna200() throws Exception {
        EstadisticasDTO dto = new EstadisticasDTO();
        dto.setTotalConsultas(-1);

        when(estadisticasService.obtenerResumen(null)).thenReturn(dto);

        mockMvc.perform(get("/estadisticas/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalConsultas").value(-1));
    }

    @Test
    @DisplayName("GET /estadisticas/consultas retorna 200 con la distribucion por estado")
    void consultas_conToken_retorna200ConDistribucion() throws Exception {
        Map<String, Object> resultado = Map.of(
                "total", 10,
                "porEstado", Map.of("PENDIENTE", 6L, "ATENDIDA", 4L)
        );
        when(estadisticasService.estadisticasConsultas("Bearer token-valido")).thenReturn(resultado);

        mockMvc.perform(get("/estadisticas/consultas").header("Authorization", "Bearer token-valido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10));
    }

    @Test
    @DisplayName("GET /estadisticas/agenda retorna 200 con la distribucion de bloques")
    void agenda_conToken_retorna200ConDistribucion() throws Exception {
        Map<String, Object> resultado = Map.of(
                "total", 54,
                "porEstado", Map.of("DISPONIBLE", 30L, "RESERVADO", 24L)
        );
        when(estadisticasService.estadisticasAgenda("Bearer token-valido")).thenReturn(resultado);

        mockMvc.perform(get("/estadisticas/agenda").header("Authorization", "Bearer token-valido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(54));
    }

    @Test
    @DisplayName("GET /estadisticas/consultas propaga el error si el servicio retorna mensaje de error")
    void consultas_servicioFalla_retorna200ConMensajeError() throws Exception {
        Map<String, Object> resultado = Map.of("error", "No se pudo conectar con ms-consultas");
        when(estadisticasService.estadisticasConsultas(any())).thenReturn(resultado);

        mockMvc.perform(get("/estadisticas/consultas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("No se pudo conectar con ms-consultas"));
    }
}
