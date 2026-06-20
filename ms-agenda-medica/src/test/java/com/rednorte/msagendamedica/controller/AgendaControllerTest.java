package com.rednorte.msagendamedica.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rednorte.msagendamedica.model.BloqueAgenda;
import com.rednorte.msagendamedica.security.JwtUtilCompartido;
import com.rednorte.msagendamedica.service.AgendaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de la capa HTTP de AgendaController.
 */
@WebMvcTest(AgendaController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests HTTP — AgendaController")
class AgendaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AgendaService agendaService;

    @MockBean
    private JwtUtilCompartido jwtUtilCompartido;

    private BloqueAgenda bloqueEjemplo;

    @BeforeEach
    void setUp() {
        bloqueEjemplo = new BloqueAgenda();
        bloqueEjemplo.setId(1L);
        bloqueEjemplo.setDoctorId("USR002");
        bloqueEjemplo.setEstablecimientoId("EST-001");
        bloqueEjemplo.setFecha(LocalDate.of(2026, 6, 22));
        bloqueEjemplo.setHoraInicio(LocalTime.of(8, 0));
        bloqueEjemplo.setHoraFin(LocalTime.of(8, 30));
        bloqueEjemplo.setEstado(BloqueAgenda.EstadoBloque.DISPONIBLE);
    }

    @Test
    @DisplayName("GET /agenda retorna la lista completa de bloques")
    void listarTodos_retorna200ConLista() throws Exception {
        when(agendaService.listarTodos()).thenReturn(List.of(bloqueEjemplo));

        mockMvc.perform(get("/agenda"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].doctorId").value("USR002"));
    }

    @Test
    @DisplayName("GET /agenda/{id} con id existente retorna 200")
    void buscarPorId_idExistente_retorna200() throws Exception {
        when(agendaService.buscarPorId(1L)).thenReturn(Optional.of(bloqueEjemplo));

        mockMvc.perform(get("/agenda/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DISPONIBLE"));
    }

    @Test
    @DisplayName("GET /agenda/{id} con id inexistente retorna 404")
    void buscarPorId_idInexistente_retorna404() throws Exception {
        when(agendaService.buscarPorId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/agenda/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /agenda/doctor/{doctorId} retorna la agenda del doctor")
    void porDoctor_retorna200ConSusBloques() throws Exception {
        when(agendaService.buscarPorDoctor("USR002")).thenReturn(List.of(bloqueEjemplo));

        mockMvc.perform(get("/agenda/doctor/USR002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /agenda/doctor/{doctorId}/fecha/{fecha} filtra por doctor y fecha")
    void porDoctorYFecha_retorna200() throws Exception {
        when(agendaService.buscarPorDoctorYFecha("USR002", LocalDate.of(2026, 6, 22)))
                .thenReturn(List.of(bloqueEjemplo));

        mockMvc.perform(get("/agenda/doctor/USR002/fecha/2026-06-22"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /agenda/disponibles/{fecha} retorna solo bloques DISPONIBLE de esa fecha")
    void disponiblesPorFecha_retorna200ConDisponibles() throws Exception {
        when(agendaService.buscarDisponiblesPorFecha(LocalDate.of(2026, 6, 22)))
                .thenReturn(List.of(bloqueEjemplo));

        mockMvc.perform(get("/agenda/disponibles/2026-06-22"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("DISPONIBLE"));
    }

    @Test
    @DisplayName("GET /agenda/paciente/{pacienteId} retorna las citas reservadas del paciente")
    void porPaciente_retorna200ConSusCitas() throws Exception {
        when(agendaService.buscarPorPaciente("USR010")).thenReturn(List.of(bloqueEjemplo));

        mockMvc.perform(get("/agenda/paciente/USR010"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /agenda crea un bloque manual y retorna 200")
    void crear_datosValidos_retorna200() throws Exception {
        when(agendaService.crear(any(BloqueAgenda.class))).thenReturn(bloqueEjemplo);

        mockMvc.perform(post("/agenda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bloqueEjemplo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value("USR002"));
    }

    @Test
    @DisplayName("POST /agenda/generar genera bloques automaticos para un doctor/fecha")
    void generarBloques_datosValidos_retorna200ConLista() throws Exception {
        Map<String, String> body = Map.of(
                "doctorId", "USR002",
                "establecimientoId", "EST-001",
                "fecha", "2026-06-22"
        );
        when(agendaService.generarBloques("USR002", "EST-001", LocalDate.of(2026, 6, 22)))
                .thenReturn(List.of(bloqueEjemplo));

        mockMvc.perform(post("/agenda/generar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("PUT /agenda/{id}/reservar reserva el bloque para un paciente")
    void reservar_bloqueDisponible_retorna200() throws Exception {
        BloqueAgenda reservado = new BloqueAgenda();
        reservado.setId(1L);
        reservado.setEstado(BloqueAgenda.EstadoBloque.RESERVADO);
        reservado.setPacienteId("USR010");

        Map<String, String> body = Map.of("pacienteId", "USR010", "consultaId", "5");
        when(agendaService.reservar(1L, "USR010", "5")).thenReturn(Optional.of(reservado));

        mockMvc.perform(put("/agenda/1/reservar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESERVADO"));
    }

    @Test
    @DisplayName("PUT /agenda/{id}/reservar sobre un bloque ya tomado retorna 400")
    void reservar_bloqueYaTomado_retorna400() throws Exception {
        Map<String, String> body = Map.of("pacienteId", "USR010", "consultaId", "5");
        when(agendaService.reservar(1L, "USR010", "5"))
                .thenThrow(new IllegalStateException("Bloque ya no esta disponible"));

        mockMvc.perform(put("/agenda/1/reservar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /agenda/{id}/reservar sobre id inexistente retorna 404")
    void reservar_idInexistente_retorna404() throws Exception {
        Map<String, String> body = Map.of("pacienteId", "USR010", "consultaId", "5");
        when(agendaService.reservar(999L, "USR010", "5")).thenReturn(Optional.empty());

        mockMvc.perform(put("/agenda/999/reservar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /agenda/{id}/cancelar marca el bloque como CANCELADO")
    void cancelar_idExistente_retorna200() throws Exception {
        BloqueAgenda cancelado = new BloqueAgenda();
        cancelado.setId(1L);
        cancelado.setEstado(BloqueAgenda.EstadoBloque.CANCELADO);

        when(agendaService.cancelar(1L)).thenReturn(Optional.of(cancelado));

        mockMvc.perform(put("/agenda/1/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));
    }

    @Test
    @DisplayName("PUT /agenda/{id}/completar marca el bloque como COMPLETADO")
    void completar_idExistente_retorna200() throws Exception {
        BloqueAgenda completado = new BloqueAgenda();
        completado.setId(1L);
        completado.setEstado(BloqueAgenda.EstadoBloque.COMPLETADO);

        when(agendaService.completar(1L)).thenReturn(Optional.of(completado));

        mockMvc.perform(put("/agenda/1/completar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADO"));
    }

    @Test
    @DisplayName("DELETE /agenda/{id} elimina el bloque y retorna 204")
    void eliminar_idValido_retorna204() throws Exception {
        doNothing().when(agendaService).eliminar(1L);

        mockMvc.perform(delete("/agenda/1"))
                .andExpect(status().isNoContent());

        verify(agendaService).eliminar(1L);
    }
}
