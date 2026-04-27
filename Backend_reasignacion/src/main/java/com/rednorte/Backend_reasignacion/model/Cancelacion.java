package com.rednorte.Backend_reasignacion.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cancelaciones")
@Data
@NoArgsConstructor
public class Cancelacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "bloque_id", referencedColumnName = "BloquesAgendaid") 
    private BloquesAgenda bloque;

    private LocalDateTime fechaCancelacion;
    private String motivo;

    @Column(name = "procesado")
    private Boolean procesado = false;

    public Cancelacion(BloquesAgenda bloque, LocalDateTime fechaCancelacion, Long id, String motivo) {
        this.bloque = bloque;
        this.fechaCancelacion = fechaCancelacion;
        this.id = id;
        this.motivo = motivo;
    }
}