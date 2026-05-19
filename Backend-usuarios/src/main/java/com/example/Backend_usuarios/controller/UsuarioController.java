package com.example.Backend_usuarios.controller;

import com.example.Backend_usuarios.dto.UsuarioRequestDTO;
import com.example.Backend_usuarios.dto.UsuarioResponseDTO;
import com.example.Backend_usuarios.model.Usuario;
import com.example.Backend_usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema RedNorte")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    private UsuarioResponseDTO toDTO(Usuario usuario) {
        if (usuario == null) return null;
        UsuarioResponseDTO.RolDTO rol = null;
        if (usuario.getRol() != null) {
            rol = new UsuarioResponseDTO.RolDTO(
                usuario.getRol().getId(),
                usuario.getRol().getTag(),
                usuario.getRol().getNombre()
            );
        }
        UsuarioResponseDTO.PersonaDTO persona = null;
        if (usuario.getPersona() != null) {
            String fecha = usuario.getPersona().getFechaNacimiento() != null
                ? usuario.getPersona().getFechaNacimiento().toString() : null;
            persona = new UsuarioResponseDTO.PersonaDTO(
                usuario.getPersona().getId(),
                usuario.getPersona().getApellido1(),
                usuario.getPersona().getApellido2(),
                usuario.getPersona().getRut(),
                fecha,
                usuario.getPersona().getSexo()
            );
        }
        return new UsuarioResponseDTO(
            usuario.getId(), usuario.getMail(), usuario.getEstado(),
            usuario.getFechaRegistro() != null ? usuario.getFechaRegistro().toString() : null,
            rol, persona
        );
    }

    @Operation(summary = "Listar todos los usuarios")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida correctamente")
    @GetMapping
    public List<UsuarioResponseDTO> usuarioListar() {
        return this.usuarioService.usuarioListar()
            .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Operation(summary = "Crear nuevo usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario creado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public void usuarioAlmacenar(@RequestBody Usuario usuario) {
        this.usuarioService.usuarioAlmacenar(usuario);
    }

    @Operation(summary = "Login de usuario", description = "Autentica con mail y contraseña. Retorna datos del usuario si las credenciales son válidas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login exitoso"),
        @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDTO> login(@RequestBody UsuarioRequestDTO u) {
        Usuario usuario = this.usuarioService.login(u.getMail(), u.getPass());
        if (usuario == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(toDTO(usuario));
    }
}
