package com.example.Backend_usuarios.controller;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.Backend_usuarios.dto.UsuarioRequestDTO;
import com.example.Backend_usuarios.dto.UsuarioResponseDTO;
import com.example.Backend_usuarios.model.Usuario;
import com.example.Backend_usuarios.service.UsuarioService;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "http://localhost:3000")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;

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

    @PostMapping
    public void usuarioAlmacenar(@RequestBody Usuario usuario) {
        this.usuarioService.usuarioAlmacenar(usuario);
    }

    @GetMapping
    public List<UsuarioResponseDTO> usuarioListar() {
        return this.usuarioService.usuarioListar()
            .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDTO> login(@RequestBody UsuarioRequestDTO u) {
        Usuario usuario = this.usuarioService.login(u.getMail(), u.getPass());
        if (usuario == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(toDTO(usuario));
    }
}
