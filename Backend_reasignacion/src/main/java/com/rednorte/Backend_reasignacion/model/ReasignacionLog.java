/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.rednorte.Backend_reasignacion.model;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "reasignaciones_log")
@Data
public class ReasignacionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cancelacion_id")
    private Cancelacion cancelacion;

    @Column(name = "paciente_id")
    private Long pacienteId; // Referencia lógica al paciente del otro microservicio

    @Column(name = "fecha_reasignacion")
    private LocalDateTime fechaReasignacion;

    private Boolean exito;

    public ReasignacionLog(Cancelacion cancelacion, Boolean exito, LocalDateTime fechaReasignacion, Long id, Long pacienteId) {
        this.cancelacion = cancelacion;
        this.exito = exito;
        this.fechaReasignacion = fechaReasignacion;
        this.id = id;
        this.pacienteId = pacienteId;
    }
}