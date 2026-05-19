package com.rednorte.msestablecimientos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "establecimiento")
public class Establecimiento {

    @Id
    @Column(name = "est_id", length = 40)
    private String id;

    @Column(name = "est_nombre", nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "est_tipo", nullable = false)
    private TipoEstablecimiento tipo;

    @Column(name = "est_direccion", nullable = false)
    private String direccion;

    @Column(name = "est_comuna", nullable = false)
    private String comuna;

    @Column(name = "est_region", nullable = false)
    private String region;

    @Column(name = "est_telefono")
    private String telefono;

    @Column(name = "est_email")
    private String email;

    @Column(name = "est_capacidad_diaria")
    private Integer capacidadDiaria;

    @Enumerated(EnumType.STRING)
    @Column(name = "est_estado", nullable = false)
    private EstadoEstablecimiento estado;

    @Column(name = "est_fecha_registro")
    private LocalDateTime fechaRegistro;

    @PrePersist
    public void prePersist() {
        if (this.fechaRegistro == null) this.fechaRegistro = LocalDateTime.now();
        if (this.estado == null) this.estado = EstadoEstablecimiento.ACTIVO;
    }

    public enum TipoEstablecimiento {
        HOSPITAL, CLINICA, CESFAM, CONSULTORIO, POSTA_RURAL
    }

    public enum EstadoEstablecimiento {
        ACTIVO, INACTIVO, EN_MANTENCION
    }

    // ─── Getters / Setters ───────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoEstablecimiento getTipo() { return tipo; }
    public void setTipo(TipoEstablecimiento tipo) { this.tipo = tipo; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getCapacidadDiaria() { return capacidadDiaria; }
    public void setCapacidadDiaria(Integer capacidadDiaria) { this.capacidadDiaria = capacidadDiaria; }

    public EstadoEstablecimiento getEstado() { return estado; }
    public void setEstado(EstadoEstablecimiento estado) { this.estado = estado; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
