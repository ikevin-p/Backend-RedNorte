package com.rednorte.bff.dto;

public class DashboardDTO {
    private int totalConsultas;
    private int consultasPendientes;
    private int consultasAgendadas;
    private int consultasAtendidas;
    private int consultasCanceladas;
    private int totalUsuarios;
    private int totalDoctores;
    private int totalPacientes;

    public DashboardDTO() {}

    public int getTotalConsultas() { return totalConsultas; }
    public void setTotalConsultas(int totalConsultas) { this.totalConsultas = totalConsultas; }
    public int getConsultasPendientes() { return consultasPendientes; }
    public void setConsultasPendientes(int consultasPendientes) { this.consultasPendientes = consultasPendientes; }
    public int getConsultasAgendadas() { return consultasAgendadas; }
    public void setConsultasAgendadas(int consultasAgendadas) { this.consultasAgendadas = consultasAgendadas; }
    public int getConsultasAtendidas() { return consultasAtendidas; }
    public void setConsultasAtendidas(int consultasAtendidas) { this.consultasAtendidas = consultasAtendidas; }
    public int getConsultasCanceladas() { return consultasCanceladas; }
    public void setConsultasCanceladas(int consultasCanceladas) { this.consultasCanceladas = consultasCanceladas; }
    public int getTotalUsuarios() { return totalUsuarios; }
    public void setTotalUsuarios(int totalUsuarios) { this.totalUsuarios = totalUsuarios; }
    public int getTotalDoctores() { return totalDoctores; }
    public void setTotalDoctores(int totalDoctores) { this.totalDoctores = totalDoctores; }
    public int getTotalPacientes() { return totalPacientes; }
    public void setTotalPacientes(int totalPacientes) { this.totalPacientes = totalPacientes; }
}
