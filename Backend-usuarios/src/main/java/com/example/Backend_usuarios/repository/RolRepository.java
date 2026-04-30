package com.example.Backend_usuarios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Backend_usuarios.model.Rol;


@Repository
public interface RolRepository extends JpaRepository<Rol, String> {
    
}
