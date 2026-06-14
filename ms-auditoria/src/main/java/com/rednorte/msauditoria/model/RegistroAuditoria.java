package com.rednorte.msauditoria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registros_auditoria")
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAccion accion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModuloSistema modulo;

    @Column(nullable = false)
    private String usuarioId;

    private String usuarioRol;

    private String recursoId;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(columnDefinition = "TEXT")
    private String detalle;

    private String ipOrigen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultadoAccion resultado;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    public enum TipoAccion {
        LOGIN, LOGOUT,
        CREAR, LEER, ACTUALIZAR, ELIMINAR,
        RESERVAR, CANCELAR, REASIGNAR, COMPLETAR
    }

    public enum ModuloSistema {
        USUARIOS, CONSULTAS, FICHA_MEDICA, AGENDA,
        ESTABLECIMIENTOS, NOTIFICACIONES, REASIGNACION
    }

    public enum ResultadoAccion {
        EXITOSO, FALLIDO, RECHAZADO
    }

    @PrePersist
    public void prePersist() {
        this.fechaHora = LocalDateTime.now();
        if (this.resultado == null) this.resultado = ResultadoAccion.EXITOSO;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TipoAccion getAccion() { return accion; }
    public void setAccion(TipoAccion accion) { this.accion = accion; }

    public ModuloSistema getModulo() { return modulo; }
    public void setModulo(ModuloSistema modulo) { this.modulo = modulo; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioRol() { return usuarioRol; }
    public void setUsuarioRol(String usuarioRol) { this.usuarioRol = usuarioRol; }

    public String getRecursoId() { return recursoId; }
    public void setRecursoId(String recursoId) { this.recursoId = recursoId; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public String getIpOrigen() { return ipOrigen; }
    public void setIpOrigen(String ipOrigen) { this.ipOrigen = ipOrigen; }

    public ResultadoAccion getResultado() { return resultado; }
    public void setResultado(ResultadoAccion resultado) { this.resultado = resultado; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}
