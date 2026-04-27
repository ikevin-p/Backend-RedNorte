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
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reasignacion_logs")
@Data
@NoArgsConstructor
public class ReasignacionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cancelacion_id")
    private Cancelacion cancelacion;

    private Boolean exito;
    private LocalDateTime fechaReasignacion;

    @Column(name = "paciente_id_nuevo")
    private Long pacienteIdNuevo;

    public ReasignacionLog(Cancelacion cancelacion, Boolean exito, LocalDateTime fechaReasignacion, Long id, Long pacienteIdNuevo) {
        this.cancelacion = cancelacion;
        this.exito = exito;
        this.fechaReasignacion = fechaReasignacion;
        this.id = id;
        this.pacienteIdNuevo = pacienteIdNuevo;
    }
}
