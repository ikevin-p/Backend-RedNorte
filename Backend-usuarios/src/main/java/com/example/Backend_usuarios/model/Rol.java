package com.example.Backend_usuarios.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    // @JsonIgnore (no @JsonBackReference): este campo nunca se serializa.
    // No es parte de un par managed/back valido porque Usuario.rol (el
    // otro lado) no tiene @JsonManagedReference -- ver el comentario
    // detallado en Usuario.java sobre por que esa anotacion era invalida
    // ahi para Jackson 3. @JsonIgnore es mas simple y correcto aqui: solo
    // se necesita excluir el campo de la serializacion, no resolver un
    // ciclo real (Usuario.rol jamas intenta serializar de vuelta a esta lista).
    @JsonIgnore
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
