package com.example.Backend_usuarios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Backend_usuarios.model.Usuario;


@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    public Usuario findByMailAndPass(String mail, String pass);
}
