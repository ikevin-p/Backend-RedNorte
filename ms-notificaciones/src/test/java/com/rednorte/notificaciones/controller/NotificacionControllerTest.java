package com.rednorte.notificaciones.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rednorte.notificaciones.model.Notificacion;
import com.rednorte.notificaciones.security.JwtUtilCompartido;
import com.rednorte.notificaciones.service.NotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de la capa HTTP de NotificacionController.
 *
 * @WebMvcTest carga solo el contexto web; se desactivan los filtros de
 * seguridad (addFilters = false) porque la autenticacion ya esta cubierta
 * por JwtAuthFilterTest.
 */
@WebMvcTest(NotificacionController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests HTTP — NotificacionController")
class NotificacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificacionService notificacionService;

    @MockBean
    private JwtUtilCompartido jwtUtilCompartido;

    private Notificacion notifEjemplo;

    @BeforeEach
    void setUp() {
        notifEjemplo = new Notificacion("USR010", "Cita confirmada", "Tu cita fue agendada", "SUCCESS", 5L);
        notifEjemplo.setId(1L);
    }

    @Test
    @DisplayName("POST /notificaciones crea una notificacion manual y retorna 200")
    void crear_datosValidos_retorna200() throws Exception {
        when(notificacionService.crear(any(Notificacion.class))).thenReturn(notifEjemplo);

        mockMvc.perform(post("/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notifEjemplo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Cita confirmada"));
    }

    @Test
    @DisplayName("POST /notificaciones/cambio-estado construye la notificacion via el service")
    void cambioEstado_datosValidos_retorna200() throws Exception {
        Map<String, Object> body = Map.of(
                "usuarioId", "USR010",
                "consultaId", 5,
                "estadoAnterior", "PENDIENTE",
                "estadoNuevo", "AGENDADA"
        );
        when(notificacionService.notificarCambioEstado("USR010", 5L, "PENDIENTE", "AGENDADA"))
                .thenReturn(notifEjemplo);

        mockMvc.perform(post("/notificaciones/cambio-estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("SUCCESS"));
    }

    @Test
    @DisplayName("POST /notificaciones/reasignacion construye la notificacion via el service")
    void reasignacion_datosValidos_retorna200() throws Exception {
        Map<String, Object> body = Map.of(
                "usuarioId", "USR010",
                "consultaId", 5,
                "especialidad", "cardiologia"
        );
        when(notificacionService.notificarReasignacion("USR010", 5L, "cardiologia"))
                .thenReturn(notifEjemplo);

        mockMvc.perform(post("/notificaciones/reasignacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /notificaciones/usuario/{id} retorna la lista completa del usuario")
    void listar_retorna200ConLista() throws Exception {
        when(notificacionService.listarPorUsuario("USR010")).thenReturn(List.of(notifEjemplo));

        mockMvc.perform(get("/notificaciones/usuario/USR010"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarioId").value("USR010"));
    }

    @Test
    @DisplayName("GET /notificaciones/usuario/{id}/no-leidas retorna solo las no leidas")
    void noLeidas_retorna200ConListaFiltrada() throws Exception {
        when(notificacionService.listarNoLeidas("USR010")).thenReturn(List.of(notifEjemplo));

        mockMvc.perform(get("/notificaciones/usuario/USR010/no-leidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /notificaciones/usuario/{id}/contador retorna el numero de no leidas")
    void contador_retorna200ConNumero() throws Exception {
        when(notificacionService.contarNoLeidas("USR010")).thenReturn(3L);

        mockMvc.perform(get("/notificaciones/usuario/USR010/contador"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noLeidas").value(3));
    }

    @Test
    @DisplayName("PUT /notificaciones/{id}/leer marca la notificacion como leida")
    void marcarLeida_idValido_retorna200() throws Exception {
        Notificacion leida = new Notificacion("USR010", "Cita confirmada", "msg", "SUCCESS", 5L);
        leida.setId(1L);
        leida.setLeida(true);

        when(notificacionService.marcarLeida(1L)).thenReturn(leida);

        mockMvc.perform(put("/notificaciones/1/leer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leida").value(true));
    }

    @Test
    @DisplayName("PUT /notificaciones/usuario/{id}/leer-todas marca todas como leidas y retorna 200")
    void marcarTodasLeidas_usuarioValido_retorna200() throws Exception {
        doNothing().when(notificacionService).marcarTodasLeidas("USR010");

        mockMvc.perform(put("/notificaciones/usuario/USR010/leer-todas"))
                .andExpect(status().isOk());

        verify(notificacionService).marcarTodasLeidas("USR010");
    }
}
