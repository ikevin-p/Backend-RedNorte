package com.example.Backend_usuarios.dto;

public class UsuarioResponseDTO {
    private String id;
    private String mail;
    private String estado;
    private String fechaRegistro;
    private RolDTO rol;
    private PersonaDTO persona;

    public static class RolDTO {
        private String id;
        private String tag;
        private String nombre;
        public RolDTO(String id, String tag, String nombre) {
            this.id = id; this.tag = tag; this.nombre = nombre;
        }
        public String getId() { return id; }
        public String getTag() { return tag; }
        public String getNombre() { return nombre; }
    }

    public static class PersonaDTO {
        private String id;
        private String apellido1;
        private String apellido2;
        private String rut;
        private String fechaNacimiento;
        private String sexo;
        public PersonaDTO(String id, String apellido1, String apellido2, String rut, String fechaNacimiento, String sexo) {
            this.id = id; this.apellido1 = apellido1; this.apellido2 = apellido2;
            this.rut = rut; this.fechaNacimiento = fechaNacimiento; this.sexo = sexo;
        }
        public String getId() { return id; }
        public String getApellido1() { return apellido1; }
        public String getApellido2() { return apellido2; }
        public String getRut() { return rut; }
        public String getFechaNacimiento() { return fechaNacimiento; }
        public String getSexo() { return sexo; }
    }

    public UsuarioResponseDTO(String id, String mail, String estado, String fechaRegistro, RolDTO rol, PersonaDTO persona) {
        this.id = id; this.mail = mail; this.estado = estado;
        this.fechaRegistro = fechaRegistro; this.rol = rol; this.persona = persona;
    }

    public String getId() { return id; }
    public String getMail() { return mail; }
    public String getEstado() { return estado; }
    public String getFechaRegistro() { return fechaRegistro; }
    public RolDTO getRol() { return rol; }
    public PersonaDTO getPersona() { return persona; }
}