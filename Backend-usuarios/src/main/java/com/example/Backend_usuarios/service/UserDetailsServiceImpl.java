package com.example.Backend_usuarios.service;

import com.example.Backend_usuarios.model.Usuario;
import com.example.Backend_usuarios.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
        Usuario usuario = this.usuarioRepository.findByMail(mail)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + mail));

        return User.builder()
            .username(usuario.getMail())
            .password(usuario.getPass())
            .roles(usuario.getRol() != null ? usuario.getRol().getTag() : "PACIENTE")
            .build();
    }
}
