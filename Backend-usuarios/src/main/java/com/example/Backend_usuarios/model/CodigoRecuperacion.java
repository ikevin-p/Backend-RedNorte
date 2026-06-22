package com.example.Backend_usuarios.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Codigo de 6 digitos para el flujo de "olvide mi contraseña".
 *
 * Se guarda en su propia tabla (no en Usuario) para no acoplar el
 * modelo principal a un dato transitorio que solo vive minutos.
 * Cada fila representa un intento de recuperacion: se invalida sola
 * por expiracion (15 min) o al usarse una vez (usado=true), lo que
 * pase primero. No se reutiliza ni se actualiza una fila existente:
 * cada solicitud nueva crea una fila nueva, así que un mismo correo
 * puede tener varios codigos activos a la vez si pidio varios (solo
 * el ultimo es realmente util, pero no hace falta borrar los viejos
 * para que el flujo funcione bien).
 */
@Entity
@Table(name = "codigo_recuperacion")
public class CodigoRecuperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cr_mail", nullable = false)
    private String mail;

    @Column(name = "cr_codigo", nullable = false, length = 6)
    private String codigo;

    @Column(name = "cr_creado", nullable = false)
    private LocalDateTime creado;

    @Column(name = "cr_expira", nullable = false)
    private LocalDateTime expira;

    @Column(name = "cr_usado", nullable = false)
    private boolean usado;

    public CodigoRecuperacion() {
        this.creado = LocalDateTime.now();
        this.usado = false;
    }

    public boolean esValido() {
        return !usado && LocalDateTime.now().isBefore(expira);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public LocalDateTime getCreado() { return creado; }
    public void setCreado(LocalDateTime creado) { this.creado = creado; }
    public LocalDateTime getExpira() { return expira; }
    public void setExpira(LocalDateTime expira) { this.expira = expira; }
    public boolean isUsado() { return usado; }
    public void setUsado(boolean usado) { this.usado = usado; }
}
