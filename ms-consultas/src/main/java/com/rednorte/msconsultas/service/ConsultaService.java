package com.rednorte.msconsultas.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rednorte.msconsultas.dto.ConsultaAdminDTO;
import com.rednorte.msconsultas.dto.ConsultaEditarDTO;
import com.rednorte.msconsultas.dto.ConsultaRequestDTO;
import com.rednorte.msconsultas.model.Consulta;
import com.rednorte.msconsultas.repository.ConsultaRepository;

@Service
public class ConsultaService {

    @Autowired
    private ConsultaRepository consultaRepository;

    // Crear una nueva consulta (el paciente la inicia)
    @Transactional
    public Consulta crearConsulta(ConsultaRequestDTO dto) {
        Consulta consulta = new Consulta();
        consulta.setUsuarioId(dto.getUsuarioId());
        consulta.setNombrePaciente(dto.getNombrePaciente());
        consulta.setSintomas(dto.getSintomas());
        consulta.setEspecialidad(dto.getEspecialidad());
        consulta.setEstado("PENDIENTE");
        consulta.setFechaCreacion(LocalDateTime.now());
        return consultaRepository.save(consulta);
    }

    // Listar todas las consultas (para el administrador)
    public List<Consulta> listarTodas() {
        return consultaRepository.findAll();
    }

    // Obtener una consulta por ID
    public Consulta obtenerPorId(Long id) {
        return consultaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consulta no encontrada con id: " + id));
    }

    // Listar consultas de un usuario específico
    public List<Consulta> listarPorUsuario(String usuarioId) {
        return consultaRepository.findByUsuarioId(usuarioId);
    }

    // El paciente edita su nombre y síntomas
    @Transactional
    public Consulta editarPorPaciente(Long id, ConsultaEditarDTO dto) {
        Consulta consulta = obtenerPorId(id);
        if (dto.getNombrePaciente() != null) {
            consulta.setNombrePaciente(dto.getNombrePaciente());
        }
        if (dto.getSintomas() != null) {
            consulta.setSintomas(dto.getSintomas());
        }
        return consultaRepository.save(consulta);
    }

    // El administrador actualiza estado, fecha de cita, bloque y notas
    @Transactional
    public Consulta actualizarPorAdmin(Long id, ConsultaAdminDTO dto) {
        Consulta consulta = obtenerPorId(id);
        if (dto.getEstado() != null) {
            consulta.setEstado(dto.getEstado());
        }
        if (dto.getFechaCita() != null) {
            consulta.setFechaCita(dto.getFechaCita());
        }
        if (dto.getBloquesAgendaId() != null) {
            consulta.setBloquesAgendaId(dto.getBloquesAgendaId());
        }
        if (dto.getNotasAdmin() != null) {
            consulta.setNotasAdmin(dto.getNotasAdmin());
        }
        return consultaRepository.save(consulta);
    }

    // Endpoint para ms-reasignacion: devuelve la consulta PENDIENTE más antigua
    // de una especialidad para reasignarle el bloque cancelado
    public Consulta obtenerPrioritario(String especialidad) {
        return consultaRepository
                .findFirstByEspecialidadAndEstadoOrderByFechaCreacionAsc(especialidad, "PENDIENTE")
                .orElseThrow(() -> new RuntimeException(
                        "No hay consultas pendientes para la especialidad: " + especialidad));
    }

    // Marcar como REASIGNADA (llamado internamente o desde ms-reasignacion)
    @Transactional
    public Consulta marcarReasignada(Long id, Long bloquesAgendaId) {
        Consulta consulta = obtenerPorId(id);
        consulta.setEstado("REASIGNADA");
        consulta.setBloquesAgendaId(bloquesAgendaId);
        return consultaRepository.save(consulta);
    }

    // Eliminar una consulta (solo admin)
    @Transactional
    public void eliminar(Long id) {
        consultaRepository.deleteById(id);
    }
}
