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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/roles")
// NOTA: el CORS lo maneja exclusivamente el API Gateway (globalcors).
@Tag(name = "Roles", description = "Gestión de roles del sistema RedNorte")
public class RolController {
    @Autowired
    private RolService rolService;

    @Operation(summary = "Crear nuevo rol")
    @PostMapping
    public void rolAlmacenar(@RequestBody Rol rol) {
        this.rolService.rolAlmacenar(rol);
    }

    @Operation(summary = "Listar todos los roles")
    @GetMapping
    public List<Rol> rolListar() {
        return this.rolService.rolListar();
    }
}
