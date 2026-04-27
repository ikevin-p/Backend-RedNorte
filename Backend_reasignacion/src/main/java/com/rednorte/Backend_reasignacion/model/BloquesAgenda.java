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
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "bloques_agenda")
@Data
/**
 *
 * @author Baryonyx
 */
public class BloquesAgenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long BloquesAgendaid;

    @Column(name = "profesional_id", nullable = false)
    private String profesionalId; // ID del médico

    @Column(name = "especialidad_id", nullable = false)
    private String especialidadId;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    public BloquesAgenda(String especialidadId, LocalDateTime fechaHora, Long id, String profesionalId) {
        this.especialidadId = especialidadId;
        this.fechaHora = fechaHora;
        this.BloquesAgendaid = id;
        this.profesionalId = profesionalId;
    }

}
