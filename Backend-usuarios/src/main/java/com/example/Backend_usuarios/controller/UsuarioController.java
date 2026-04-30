package com.example.Backend_usuarios.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Backend_usuarios.dto.UsuarioRequestDTO;
import com.example.Backend_usuarios.model.Usuario;
import com.example.Backend_usuarios.service.UsuarioService;



@RestController
@RequestMapping("/usuarios")
@CrossOrigin
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public void usuarioAlmacenar(@RequestBody Usuario usuario) {
        this.usuarioService.usuarioAlmacenar(usuario);
    }

    @GetMapping
    public List<Usuario> usuarioListar() {
        return this.usuarioService.usuarioListar();
    }

    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody UsuarioRequestDTO u) {
        Usuario usuario = this.usuarioService.login(u.getMail(), u.getPass());

        return ResponseEntity.ok(usuario);
    }

    // @PostMapping
    // public void 
}
