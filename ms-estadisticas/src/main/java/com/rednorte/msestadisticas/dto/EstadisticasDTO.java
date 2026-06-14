package com.rednorte.msestadisticas.dto;

import java.util.Map;

public class EstadisticasDTO {

    private long totalConsultas;
    private long totalUsuarios;
    private long totalBloquesAgenda;
    private long totalEstablecimientos;

    private Map<String, Long> consultasPorEstado;
    private Map<String, Long> consultasPorDoctor;
    private Map<String, Long> bloquesPorEstado;
    private Map<String, Long> establecimientosPorTipo;

    // Constructor vacío
    public EstadisticasDTO() {}

    // Getters y Setters
    public long getTotalConsultas() { return totalConsultas; }
    public void setTotalConsultas(long totalConsultas) { this.totalConsultas = totalConsultas; }

    public long getTotalUsuarios() { return totalUsuarios; }
    public void setTotalUsuarios(long totalUsuarios) { this.totalUsuarios = totalUsuarios; }

    public long getTotalBloquesAgenda() { return totalBloquesAgenda; }
    public void setTotalBloquesAgenda(long totalBloquesAgenda) { this.totalBloquesAgenda = totalBloquesAgenda; }

    public long getTotalEstablecimientos() { return totalEstablecimientos; }
    public void setTotalEstablecimientos(long totalEstablecimientos) { this.totalEstablecimientos = totalEstablecimientos; }

    public Map<String, Long> getConsultasPorEstado() { return consultasPorEstado; }
    public void setConsultasPorEstado(Map<String, Long> consultasPorEstado) { this.consultasPorEstado = consultasPorEstado; }

    public Map<String, Long> getConsultasPorDoctor() { return consultasPorDoctor; }
    public void setConsultasPorDoctor(Map<String, Long> consultasPorDoctor) { this.consultasPorDoctor = consultasPorDoctor; }

    public Map<String, Long> getBloquesPorEstado() { return bloquesPorEstado; }
    public void setBloquesPorEstado(Map<String, Long> bloquesPorEstado) { this.bloquesPorEstado = bloquesPorEstado; }

    public Map<String, Long> getEstablecimientosPorTipo() { return establecimientosPorTipo; }
    public void setEstablecimientosPorTipo(Map<String, Long> establecimientosPorTipo) { this.establecimientosPorTipo = establecimientosPorTipo; }
}
