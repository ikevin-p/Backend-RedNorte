package com.rednorte.msauditoria.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rednorte.msauditoria.model.RegistroAuditoria;
import com.rednorte.msauditoria.security.JwtUtilCompartido;
import com.rednorte.msauditoria.service.AuditoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de la capa HTTP de AuditoriaController.
 */
@WebMvcTest(AuditoriaController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests HTTP — AuditoriaController")
class AuditoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuditoriaService auditoriaService;

    @MockBean
    private JwtUtilCompartido jwtUtilCompartido;

    private RegistroAuditoria registroEjemplo;

    @BeforeEach
    void setUp() {
        registroEjemplo = new RegistroAuditoria();
        registroEjemplo.setId(1L);
        registroEjemplo.setAccion(RegistroAuditoria.TipoAccion.LOGIN);
        registroEjemplo.setModulo(RegistroAuditoria.ModuloSistema.USUARIOS);
        registroEjemplo.setUsuarioId("USR010");
        registroEjemplo.setResultado(RegistroAuditoria.ResultadoAccion.EXITOSO);
        registroEjemplo.setFechaHora(LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /auditoria registra un nuevo evento y retorna 200")
    void registrar_datosValidos_retorna200() throws Exception {
        when(auditoriaService.registrar(any(RegistroAuditoria.class))).thenReturn(registroEjemplo);

        mockMvc.perform(post("/auditoria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroEjemplo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value("USR010"));
    }

    @Test
    @DisplayName("GET /auditoria retorna los 100 registros mas recientes")
    void listarRecientes_retorna200ConLista() throws Exception {
        when(auditoriaService.listarRecientes()).thenReturn(List.of(registroEjemplo));

        mockMvc.perform(get("/auditoria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /auditoria/{id} con id existente retorna 200")
    void porId_idExistente_retorna200() throws Exception {
        when(auditoriaService.buscarPorId(1L)).thenReturn(Optional.of(registroEjemplo));

        mockMvc.perform(get("/auditoria/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accion").value("LOGIN"));
    }

    @Test
    @DisplayName("GET /auditoria/{id} con id inexistente retorna 404")
    void porId_idInexistente_retorna404() throws Exception {
        when(auditoriaService.buscarPorId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/auditoria/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /auditoria/usuario/{usuarioId} retorna el historial del usuario")
    void porUsuario_retorna200ConHistorial() throws Exception {
        when(auditoriaService.porUsuario("USR010")).thenReturn(List.of(registroEjemplo));

        mockMvc.perform(get("/auditoria/usuario/USR010"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarioId").value("USR010"));
    }

    @Test
    @DisplayName("GET /auditoria/modulo/{modulo} con modulo valido retorna 200")
    void porModulo_moduloValido_retorna200() throws Exception {
        when(auditoriaService.porModulo("USUARIOS")).thenReturn(List.of(registroEjemplo));

        mockMvc.perform(get("/auditoria/modulo/USUARIOS"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /auditoria/modulo/{modulo} con modulo invalido retorna 400")
    void porModulo_moduloInvalido_retorna400() throws Exception {
        when(auditoriaService.porModulo("INVENTADO")).thenThrow(new IllegalArgumentException());

        mockMvc.perform(get("/auditoria/modulo/INVENTADO"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /auditoria/accion/{accion} con accion valida retorna 200")
    void porAccion_accionValida_retorna200() throws Exception {
        when(auditoriaService.porAccion("LOGIN")).thenReturn(List.of(registroEjemplo));

        mockMvc.perform(get("/auditoria/accion/LOGIN"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /auditoria/accion/{accion} con accion invalida retorna 400")
    void porAccion_accionInvalida_retorna400() throws Exception {
        when(auditoriaService.porAccion("INVENTADA")).thenThrow(new IllegalArgumentException());

        mockMvc.perform(get("/auditoria/accion/INVENTADA"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /auditoria/resultado/{resultado} con resultado valido retorna 200")
    void porResultado_resultadoValido_retorna200() throws Exception {
        when(auditoriaService.porResultado("EXITOSO")).thenReturn(List.of(registroEjemplo));

        mockMvc.perform(get("/auditoria/resultado/EXITOSO"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /auditoria/resultado/{resultado} con resultado invalido retorna 400")
    void porResultado_resultadoInvalido_retorna400() throws Exception {
        when(auditoriaService.porResultado("INVENTADO")).thenThrow(new IllegalArgumentException());

        mockMvc.perform(get("/auditoria/resultado/INVENTADO"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /auditoria/rango retorna los registros del rango de fechas")
    void porRango_fechasValidas_retorna200() throws Exception {
        when(auditoriaService.porRango(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(registroEjemplo));

        mockMvc.perform(get("/auditoria/rango")
                        .param("desde", "2026-06-01T00:00:00")
                        .param("hasta", "2026-06-30T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
