/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.Backend_usuarios.dto;

/**
 *
 * @author Baryonyx
 */
public class UsuarioRequestDTO {
    private String mail;
    private String pass;

    public UsuarioRequestDTO() {
        this.mail = "";
        this.pass = "";
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
}
