package com.example.Backend_usuarios.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class Usuario {
    @Id
    @Column(name = "u_id")
    private String id;

    @Column(name = "u_mail")
    private String mail;

    @Column(name = "u_pass")
    private String pass;

    @Column(name = "u_estado")
    private String estado;

    @Column(name = "u_fecha_registro")
    private LocalDateTime fechaRegistro;

    @OneToOne(mappedBy = "usuario")
    private Persona persona;

    @ManyToOne
    @JoinColumn(name = "u_rol_id")
    private Rol rol;

    public Usuario() {
        this.id = "";
        this.mail = "";
        this.pass = "";
        this.estado = "";
        this.fechaRegistro = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
