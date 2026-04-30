package com.example.Backend_usuarios.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Backend_usuarios.model.Rol;
import com.example.Backend_usuarios.service.RolService;


@RestController
@RequestMapping("/roles")
@CrossOrigin
public class RolController {
    @Autowired
    private RolService rolService;

    @PostMapping
    public void rolAlmacenar(@RequestBody Rol rol) {
        this.rolService.rolAlmacenar(rol);
    }

    @GetMapping
    public List<Rol> rolListar() {
        return this.rolService.rolListar();
    }
}
