package com.example.Backend_usuarios.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

    // @JsonManagedReference: este lado SI se serializa normalmente.
    // Corta el ciclo infinito Usuario -> Persona -> Usuario -> ...
    // (causaba "Document nesting depth exceeds the maximum allowed" en
    // GET /usuarios al listar todos los usuarios).
    // Se nombra explicitamente "persona-usuario" porque hay DOS pares
    // de referencias en esta clase (Persona y Rol). Sin nombre, ambos
    // usan el id por defecto "defaultReference" y Jackson 3 (paquete
    // tools.jackson.databind, viene con Spring Boot 4) los confunde
    // entre si, lanzando InvalidDefinitionException al serializar -
    // bug real detectado: rompia POST /usuarios y GET /usuarios/login
    // con un 500 que Spring enmascaraba como 403 al redirigir a /error.
    @JsonManagedReference("persona-usuario")
    @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY)
    private Persona persona;

    // Sin anotacion de referencia: a diferencia de persona (arriba), este
    // campo NO genera un ciclo real al serializar, porque Rol.usuarios ya
    // esta excluido con @JsonBackReference("rol-usuario") del otro lado.
    // Usar @JsonManagedReference aqui es invalido en Jackson 3 (paquete
    // tools.jackson.databind, viene con Spring Boot 4): esa anotacion
    // exige que el lado "back" sea una coleccion compatible con el tipo
    // administrado, y aqui Usuario.rol es un objeto simple, no una lista
    // de Rol. Bug real detectado: con la anotacion presente, Jackson
    // lanzaba InvalidDefinitionException tanto al leer como al escribir
    // JSON de Usuario, rompiendo POST /usuarios y GET /usuarios/login
    // con un 500 que Spring enmascaraba como 403 al redirigir a /error.
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
