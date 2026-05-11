package com.rednorte.msficha.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ficha_medica")
public class FichaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", unique = true, nullable = false)
    private String usuarioId;

    // Datos físicos
    private Double estatura;
    private Double peso;
    private String grupoSanguineo;
    private String presionArterial;
    private String frecuenciaCardiaca;
    private String glucosa;

    // Contacto
    private String telefono;
    private String direccion;

    // Información médica
    @Column(columnDefinition = "TEXT")
    private String alergias;

    @Column(columnDefinition = "TEXT")
    private String condicionesCronicas;

    @Column(columnDefinition = "TEXT")
    private String medicamentosActuales;

    @Column(columnDefinition = "TEXT")
    private String cirugiasPrevias;

    @Column(columnDefinition = "TEXT")
    private String antecedentesFamiliares;

    private String habitoTabaco;
    private String habitoAlcohol;

    // Contacto de emergencia
    private String emergenciaNombre;
    private String emergenciaTelefono;
    private String emergenciaRelacion;

    public FichaMedica() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    public Double getEstatura() { return estatura; }
    public void setEstatura(Double estatura) { this.estatura = estatura; }
    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }
    public String getGrupoSanguineo() { return grupoSanguineo; }
    public void setGrupoSanguineo(String grupoSanguineo) { this.grupoSanguineo = grupoSanguineo; }
    public String getPresionArterial() { return presionArterial; }
    public void setPresionArterial(String presionArterial) { this.presionArterial = presionArterial; }
    public String getFrecuenciaCardiaca() { return frecuenciaCardiaca; }
    public void setFrecuenciaCardiaca(String frecuenciaCardiaca) { this.frecuenciaCardiaca = frecuenciaCardiaca; }
    public String getGlucosa() { return glucosa; }
    public void setGlucosa(String glucosa) { this.glucosa = glucosa; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }
    public String getCondicionesCronicas() { return condicionesCronicas; }
    public void setCondicionesCronicas(String condicionesCronicas) { this.condicionesCronicas = condicionesCronicas; }
    public String getMedicamentosActuales() { return medicamentosActuales; }
    public void setMedicamentosActuales(String medicamentosActuales) { this.medicamentosActuales = medicamentosActuales; }
    public String getCirugiasPrevias() { return cirugiasPrevias; }
    public void setCirugiasPrevias(String cirugiasPrevias) { this.cirugiasPrevias = cirugiasPrevias; }
    public String getAntecedentesFamiliares() { return antecedentesFamiliares; }
    public void setAntecedentesFamiliares(String antecedentesFamiliares) { this.antecedentesFamiliares = antecedentesFamiliares; }
    public String getHabitoTabaco() { return habitoTabaco; }
    public void setHabitoTabaco(String habitoTabaco) { this.habitoTabaco = habitoTabaco; }
    public String getHabitoAlcohol() { return habitoAlcohol; }
    public void setHabitoAlcohol(String habitoAlcohol) { this.habitoAlcohol = habitoAlcohol; }
    public String getEmergenciaNombre() { return emergenciaNombre; }
    public void setEmergenciaNombre(String emergenciaNombre) { this.emergenciaNombre = emergenciaNombre; }
    public String getEmergenciaTelefono() { return emergenciaTelefono; }
    public void setEmergenciaTelefono(String emergenciaTelefono) { this.emergenciaTelefono = emergenciaTelefono; }
    public String getEmergenciaRelacion() { return emergenciaRelacion; }
    public void setEmergenciaRelacion(String emergenciaRelacion) { this.emergenciaRelacion = emergenciaRelacion; }
}
