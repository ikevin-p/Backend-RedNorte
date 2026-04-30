package com.example.Backend_usuarios.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

    public void usuarioAlmacenar(Usuario usuario) {
        Persona p = usuario.getPersona();
        p.setUsuario(usuario);

        this.usuarioRepository.save(usuario);
    }

    public List<Usuario> usuarioListar(){ 
        return this.usuarioRepository.findAll();
    }

    public void usuarioRolAlmacenarA(String usuarioID, String rolID) {
        Usuario u = this.usuarioRepository.findById(usuarioID).get();
        Rol r = this.rolRepository.findById(rolID).get();

        u.setRol(r);
        this.usuarioRepository.save(u);
    }

    public Usuario login(String mail, String pass) {
        return this.usuarioRepository.findByMailAndPass(mail, pass);
    }

}
