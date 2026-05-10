package com.rednorte.msconsultas.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "consultas")
@Data
@NoArgsConstructor
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referencia al usuario en ms-usuarios (solo guardamos el ID, sin llamada HTTP)
    @Column(name = "usuario_id", nullable = false)
    private String usuarioId;

    // El paciente puede editar estos campos desde el frontend
    @Column(name = "nombre_paciente")
    private String nombrePaciente;

    @Column(name = "sintomas", columnDefinition = "TEXT")
    private String sintomas;

    // Especialidad médica solicitada (ej: "cardiologia", "pediatria")
    @Column(name = "especialidad", nullable = false)
    private String especialidad;

    // Estado: PENDIENTE, AGENDADA, CANCELADA, REASIGNADA, ATENDIDA
    @Column(name = "estado", nullable = false)
    private String estado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    // Fecha de la cita asignada (puede ser null si aún está pendiente)
    @Column(name = "fecha_cita")
    private LocalDateTime fechaCita;

    // Referencia al bloque de agenda de ms-reasignacion (para vincular la cita)
    @Column(name = "bloques_agenda_id")
    private Long bloquesAgendaId;

    // Notas adicionales del administrador
    @Column(name = "notas_admin", columnDefinition = "TEXT")
    private String notasAdmin;
}
