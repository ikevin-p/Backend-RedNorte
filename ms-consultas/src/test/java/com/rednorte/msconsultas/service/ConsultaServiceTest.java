package com.rednorte.msconsultas.service;

import com.rednorte.msconsultas.dto.ConsultaAdminDTO;
import com.rednorte.msconsultas.dto.ConsultaEditarDTO;
import com.rednorte.msconsultas.dto.ConsultaRequestDTO;
import com.rednorte.msconsultas.model.Consulta;
import com.rednorte.msconsultas.repository.ConsultaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios — ConsultaService")
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @InjectMocks
    private ConsultaService consultaService;

    private Consulta consultaMock;

    @BeforeEach
    void setUp() {
        consultaMock = new Consulta();
        consultaMock.setId(1L);
        consultaMock.setUsuarioId("USR001");
        consultaMock.setNombrePaciente("Juan Perez");
        consultaMock.setSintomas("Dolor de cabeza");
        consultaMock.setEspecialidad("cardiologia");
        consultaMock.setEstado("PENDIENTE");
        consultaMock.setFechaCreacion(LocalDateTime.now());
    }

    // ─── CREAR ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Crear consulta queda en estado PENDIENTE")
    void crearConsulta_quedaEnEstadoPendiente() {
        ConsultaRequestDTO dto = new ConsultaRequestDTO();
        dto.setUsuarioId("USR002");
        dto.setNombrePaciente("Maria Lopez");
        dto.setSintomas("Fiebre");
        dto.setEspecialidad("pediatria");

        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

        Consulta resultado = consultaService.crearConsulta(dto);

        assertEquals("PENDIENTE", resultado.getEstado());
        assertEquals("USR002", resultado.getUsuarioId());
        assertEquals("pediatria", resultado.getEspecialidad());
        assertNotNull(resultado.getFechaCreacion());
        verify(consultaRepository, times(1)).save(any(Consulta.class));
    }

    // ─── LISTAR ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Listar todas retorna lista completa")
    void listarTodas_retornaListaCompleta() {
        Consulta otra = new Consulta();
        otra.setId(2L);
        when(consultaRepository.findAll()).thenReturn(List.of(consultaMock, otra));

        List<Consulta> resultado = consultaService.listarTodas();

        assertEquals(2, resultado.size());
        verify(consultaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Listar por usuario retorna solo las consultas de ese usuario")
    void listarPorUsuario_retornaConsultasDelUsuario() {
        when(consultaRepository.findByUsuarioId("USR001")).thenReturn(List.of(consultaMock));

        List<Consulta> resultado = consultaService.listarPorUsuario("USR001");

        assertEquals(1, resultado.size());
        assertEquals("USR001", resultado.get(0).getUsuarioId());
    }

    // ─── OBTENER POR ID ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Obtener por ID existente retorna la consulta")
    void obtenerPorId_idExistente_retornaConsulta() {
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consultaMock));

        Consulta resultado = consultaService.obtenerPorId(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("Juan Perez", resultado.getNombrePaciente());
    }

    @Test
    @DisplayName("Obtener por ID inexistente lanza excepcion")
    void obtenerPorId_idInexistente_lanzaExcepcion() {
        when(consultaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> consultaService.obtenerPorId(99L));

        assertTrue(ex.getMessage().contains("99"));
    }

    // ─── EDITAR POR PACIENTE ─────────────────────────────────────────────────

    @Test
    @DisplayName("Editar por paciente actualiza nombre y sintomas")
    void editarPorPaciente_actualizaNombreYSintomas() {
        ConsultaEditarDTO dto = new ConsultaEditarDTO();
        dto.setNombrePaciente("Juan Perez Editado");
        dto.setSintomas("Dolor de espalda");

        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consultaMock));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

        Consulta resultado = consultaService.editarPorPaciente(1L, dto);

        assertEquals("Juan Perez Editado", resultado.getNombrePaciente());
        assertEquals("Dolor de espalda", resultado.getSintomas());
    }

    @Test
    @DisplayName("Editar por paciente con campos nulos no sobreescribe los valores existentes")
    void editarPorPaciente_camposNulos_noSobreescribe() {
        ConsultaEditarDTO dto = new ConsultaEditarDTO();
        dto.setNombrePaciente(null);
        dto.setSintomas(null);

        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consultaMock));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

        Consulta resultado = consultaService.editarPorPaciente(1L, dto);

        assertEquals("Juan Perez", resultado.getNombrePaciente());
        assertEquals("Dolor de cabeza", resultado.getSintomas());
    }

    // ─── ACTUALIZAR POR ADMIN ────────────────────────────────────────────────

    @Test
    @DisplayName("Actualizar por admin cambia estado y fecha de cita")
    void actualizarPorAdmin_cambiaEstadoYFechaCita() {
        LocalDateTime nuevaFecha = LocalDateTime.now().plusDays(3);
        ConsultaAdminDTO dto = new ConsultaAdminDTO();
        dto.setEstado("AGENDADA");
        dto.setFechaCita(nuevaFecha);
        dto.setNotasAdmin("Confirmado con el doctor");

        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consultaMock));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

        Consulta resultado = consultaService.actualizarPorAdmin(1L, dto);

        assertEquals("AGENDADA", resultado.getEstado());
        assertEquals(nuevaFecha, resultado.getFechaCita());
        assertEquals("Confirmado con el doctor", resultado.getNotasAdmin());
    }

    // ─── PRIORITARIO (para ms-reasignacion) ─────────────────────────────────

    @Test
    @DisplayName("Obtener prioritario retorna la consulta pendiente mas antigua")
    void obtenerPrioritario_retornaConsultaMasAntigua() {
        when(consultaRepository.findFirstByEspecialidadAndEstadoOrderByFechaCreacionAsc(
                "cardiologia", "PENDIENTE")).thenReturn(Optional.of(consultaMock));

        Consulta resultado = consultaService.obtenerPrioritario("cardiologia");

        assertEquals("cardiologia", resultado.getEspecialidad());
        assertEquals("PENDIENTE", resultado.getEstado());
    }

    @Test
    @DisplayName("Obtener prioritario sin consultas pendientes lanza excepcion")
    void obtenerPrioritario_sinPendientes_lanzaExcepcion() {
        when(consultaRepository.findFirstByEspecialidadAndEstadoOrderByFechaCreacionAsc(
                "pediatria", "PENDIENTE")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> consultaService.obtenerPrioritario("pediatria"));
    }

    // ─── MARCAR REASIGNADA ───────────────────────────────────────────────────

    @Test
    @DisplayName("Marcar reasignada actualiza estado y bloque de agenda")
    void marcarReasignada_actualizaEstadoYBloqueAgenda() {
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consultaMock));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

        Consulta resultado = consultaService.marcarReasignada(1L, 55L);

        assertEquals("REASIGNADA", resultado.getEstado());
        assertEquals(55L, resultado.getBloquesAgendaId());
    }

    // ─── ELIMINAR ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Eliminar consulta invoca al repositorio con el ID correcto")
    void eliminar_invocaRepositorioConIdCorrecto() {
        doNothing().when(consultaRepository).deleteById(anyLong());

        consultaService.eliminar(1L);

        verify(consultaRepository, times(1)).deleteById(1L);
    }
}
