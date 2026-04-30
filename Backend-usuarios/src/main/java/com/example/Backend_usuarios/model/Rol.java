package com.example.Backend_usuarios.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Rol {
    @Id
    @Column(name = "r_id")
    private String id;

    @Column(name = "r_tag")
    private String tag;

    @Column(name = "r_nombre")
    private String nombre;

    @Column(name = "r_descripcion")
    private String descripcion;

    @Column(name = "r_estado")
    private String estado;   
    
    @OneToMany(mappedBy = "rol")
    private List<Usuario> usuarios;

    public Rol() {
        this.id = "";
        this.tag = "";
        this.nombre = "";
        this.descripcion = "";
        this.estado = "";
        this.usuarios = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
}
