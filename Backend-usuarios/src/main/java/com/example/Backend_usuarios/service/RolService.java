package com.example.Backend_usuarios.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.Backend_usuarios.model.Rol;
import com.example.Backend_usuarios.repository.RolRepository;


@Service
public class RolService {
    @Autowired
    private RolRepository rolRepository;

    public void rolAlmacenar(Rol rol) {
        this.rolRepository.save(rol);
    }

    public List<Rol> rolListar() {
        return this.rolRepository.findAll();
    }
}
