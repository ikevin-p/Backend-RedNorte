package com.rednorte.msconsultas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rednorte.msconsultas.dto.ConsultaAdminDTO;
import com.rednorte.msconsultas.dto.ConsultaEditarDTO;
import com.rednorte.msconsultas.dto.ConsultaRequestDTO;
import com.rednorte.msconsultas.model.Consulta;
import com.rednorte.msconsultas.security.JwtUtilCompartido;
import com.rednorte.msconsultas.service.ConsultaService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de la capa HTTP de ConsultaController.
 *
 * @WebMvcTest carga solo el contexto web (controller + manejo de excepciones),
 * sin levantar JPA ni conexion a base de datos real. Se desactivan los
 * filtros de seguridad (addFilters = false) porque aqui el objetivo es
 * probar el contrato HTTP/JSON del controller, no la autenticacion
 * (eso ya esta cubierto por JwtAuthFilterTest).
 */
@WebMvcTest(ConsultaController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests HTTP — ConsultaController")
class ConsultaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConsultaService consultaService;

    // JwtAuthFilter (escaneado por @WebMvcTest al ser @Component) depende de
    // JwtUtilCompartido en su constructor. Aunque addFilters=false evita que
    // el filtro se ejecute, Spring igual necesita poder *construir* el bean
    // para armar el contexto del test, asi que se mockea su dependencia.
    @MockBean
    private JwtUtilCompartido jwtUtilCompartido;

    private Consulta consultaEjemplo;

    @BeforeEach
    void setUp() {
        consultaEjemplo = new Consulta();
        consultaEjemplo.setId(1L);
        consultaEjemplo.setUsuarioId("USR010");
        consultaEjemplo.setNombrePaciente("Juan Perez");
        consultaEjemplo.setSintomas("Dolor de cabeza persistente");
        consultaEjemplo.setEspecialidad("medicina general");
        consultaEjemplo.setEstado("PENDIENTE");
        consultaEjemplo.setFechaCreacion(LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /consultas con datos validos retorna 200 y la consulta creada")
    void crear_datosValidos_retorna200ConConsulta() throws Exception {
        ConsultaRequestDTO dto = new ConsultaRequestDTO();
        dto.setUsuarioId("USR010");
        dto.setNombrePaciente("Juan Perez");
        dto.setSintomas("Dolor de cabeza persistente");
        dto.setEspecialidad("medicina general");

        when(consultaService.crearConsulta(any(ConsultaRequestDTO.class))).thenReturn(consultaEjemplo);

        mockMvc.perform(post("/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    @DisplayName("POST /consultas sin especialidad retorna 400 con el error de validacion")
    void crear_sinEspecialidad_retorna400ConError() throws Exception {
        ConsultaRequestDTO dto = new ConsultaRequestDTO();
        dto.setUsuarioId("USR010");
        dto.setNombrePaciente("Juan Perez");
        dto.setSintomas("Dolor de cabeza");

        mockMvc.perform(post("/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.especialidad").exists());

        verify(consultaService, never()).crearConsulta(any());
    }

    @Test
    @DisplayName("GET /consultas retorna 200 con la lista completa")
    void listarTodas_retorna200ConLista() throws Exception {
        when(consultaService.listarTodas()).thenReturn(List.of(consultaEjemplo));

        mockMvc.perform(get("/consultas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombrePaciente").value("Juan Perez"));
    }

    @Test
    @DisplayName("GET /consultas/{id} con id existente retorna 200 con la consulta")
    void obtenerPorId_idExistente_retorna200() throws Exception {
        when(consultaService.obtenerPorId(1L)).thenReturn(consultaEjemplo);

        mockMvc.perform(get("/consultas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.especialidad").value("medicina general"));
    }

    @Test
    @DisplayName("GET /consultas/{id} con id inexistente retorna 404")
    void obtenerPorId_idInexistente_retorna404() throws Exception {
        when(consultaService.obtenerPorId(999L))
                .thenThrow(new RuntimeException("Consulta no encontrada con id: 999"));

        mockMvc.perform(get("/consultas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Consulta no encontrada con id: 999"));
    }

    @Test
    @DisplayName("GET /consultas/usuario/{usuarioId} retorna solo las consultas de ese usuario")
    void listarPorUsuario_retorna200ConSusConsultas() throws Exception {
        when(consultaService.listarPorUsuario("USR010")).thenReturn(List.of(consultaEjemplo));

        mockMvc.perform(get("/consultas/usuario/USR010"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarioId").value("USR010"));
    }

    @Test
    @DisplayName("PUT /consultas/{id}/paciente actualiza nombre y sintomas")
    void editarPaciente_datosValidos_retorna200Actualizado() throws Exception {
        ConsultaEditarDTO dto = new ConsultaEditarDTO();
        dto.setNombrePaciente("Juan Perez Castro");
        dto.setSintomas("Dolor de cabeza intenso desde hace 3 dias");

        Consulta actualizada = new Consulta();
        actualizada.setId(1L);
        actualizada.setNombrePaciente("Juan Perez Castro");
        actualizada.setSintomas("Dolor de cabeza intenso desde hace 3 dias");
        actualizada.setEstado("PENDIENTE");

        when(consultaService.editarPorPaciente(eq(1L), any(ConsultaEditarDTO.class))).thenReturn(actualizada);

        mockMvc.perform(put("/consultas/1/paciente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombrePaciente").value("Juan Perez Castro"));
    }

    @Test
    @DisplayName("PUT /consultas/{id}/admin actualiza estado y notas")
    void actualizarAdmin_datosValidos_retorna200Actualizado() throws Exception {
        ConsultaAdminDTO dto = new ConsultaAdminDTO();
        dto.setEstado("AGENDADA");
        dto.setNotasAdmin("Cita confirmada con Dr. Vega");

        Consulta actualizada = new Consulta();
        actualizada.setId(1L);
        actualizada.setEstado("AGENDADA");
        actualizada.setNotasAdmin("Cita confirmada con Dr. Vega");

        when(consultaService.actualizarPorAdmin(eq(1L), any(ConsultaAdminDTO.class))).thenReturn(actualizada);

        mockMvc.perform(put("/consultas/1/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("AGENDADA"));
    }

    @Test
    @DisplayName("GET /consultas/prioritario/{especialidad} retorna el id de la consulta mas antigua")
    void obtenerPrioritario_existeConsultaPendiente_retornaSuId() throws Exception {
        when(consultaService.obtenerPrioritario("cardiologia")).thenReturn(consultaEjemplo);

        mockMvc.perform(get("/consultas/prioritario/cardiologia"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("GET /consultas/prioritario/{especialidad} sin pendientes retorna 404")
    void obtenerPrioritario_sinPendientes_retorna404() throws Exception {
        when(consultaService.obtenerPrioritario("neurologia"))
                .thenThrow(new RuntimeException("No hay consultas pendientes para la especialidad: neurologia"));

        mockMvc.perform(get("/consultas/prioritario/neurologia"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /consultas/{id}/reasignar marca la consulta como REASIGNADA")
    void reasignar_idValido_retorna200ConEstadoReasignada() throws Exception {
        Consulta reasignada = new Consulta();
        reasignada.setId(1L);
        reasignada.setEstado("REASIGNADA");
        reasignada.setBloquesAgendaId(55L);

        when(consultaService.marcarReasignada(eq(1L), eq(55L))).thenReturn(reasignada);

        mockMvc.perform(put("/consultas/1/reasignar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("55"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REASIGNADA"));
    }

    @Test
    @DisplayName("DELETE /consultas/{id} retorna 204 sin contenido")
    void eliminar_idValido_retorna204() throws Exception {
        doNothing().when(consultaService).eliminar(1L);

        mockMvc.perform(delete("/consultas/1"))
                .andExpect(status().isNoContent());

        verify(consultaService).eliminar(1L);
    }
}
