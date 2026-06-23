package com.example.Backend_usuarios.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.Backend_usuarios.model.Persona;
import com.example.Backend_usuarios.model.Rol;
import com.example.Backend_usuarios.model.Usuario;
import com.example.Backend_usuarios.repository.RolRepository;
import com.example.Backend_usuarios.repository.UsuarioRepository;


@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public void usuarioAlmacenar(Usuario usuario) {
        Persona p = usuario.getPersona();
        p.setUsuario(usuario);

        // Hashear contraseña antes de persistir
        usuario.setPass(passwordEncoder.encode(usuario.getPass()));

        this.usuarioRepository.save(usuario);
    }

    public List<Usuario> usuarioListar(){ 
        return this.usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(String id) {
        return this.usuarioRepository.findById(id);
    }

    public void usuarioRolAlmacenarA(String usuarioID, String rolID) {
        Usuario u = this.usuarioRepository.findById(usuarioID).get();
        Rol r = this.rolRepository.findById(rolID).get();

        u.setRol(r);
        this.usuarioRepository.save(u);
    }

    public Usuario login(String mail, String pass) {
        // 1. Buscar por mail
        Usuario usuario = this.usuarioRepository.findByMail(mail).orElse(null);
        if (usuario == null) return null;

        // 2. Comparar texto plano vs hash BCrypt almacenado
        boolean passValida = passwordEncoder.matches(pass, usuario.getPass());
        if (!passValida) return null;

        return usuario;
    }

}
