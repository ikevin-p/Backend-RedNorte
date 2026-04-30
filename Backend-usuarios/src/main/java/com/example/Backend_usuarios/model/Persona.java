package com.example.Backend_usuarios.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class Persona {
    @Id
    @Column(name = "p_id")
    private String id;

    @Column(name = "p_rut")
    private String rut;

    @Column(name = "p_apellido_1")
    private String apellido1;

    @Column(name = "p_apellido_2")
    private String apellido2;

    @Column(name = "p_fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "p_sexo")
    private String sexo;

    @OneToOne
    @JoinColumn(name = "p_usuario_id")
    private Usuario usuario;

    public Persona() {
        this.id = "";
        this.rut = "";
        this.apellido1 = "";
        this.apellido2 = "";
        this.fechaNacimiento = LocalDate.now();
        this.sexo = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getApellido1() {
        return apellido1;
    }

    public void setApellido1(String apellido1) {
        this.apellido1 = apellido1;
    }

    public String getApellido2() {
        return apellido2;
    }

    public void setApellido2(String apellido2) {
        this.apellido2 = apellido2;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
