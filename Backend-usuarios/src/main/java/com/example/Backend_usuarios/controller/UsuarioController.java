package com.example.Backend_usuarios.controller;

import com.example.Backend_usuarios.dto.UsuarioRequestDTO;
import com.example.Backend_usuarios.dto.UsuarioResponseDTO;
import com.example.Backend_usuarios.model.Usuario;
import com.example.Backend_usuarios.security.JwtUtil;
import com.example.Backend_usuarios.service.RecuperacionService;
import com.example.Backend_usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
// NOTA: el CORS lo maneja exclusivamente el API Gateway (globalcors).
// @CrossOrigin aqui duplica el header Access-Control-Allow-Origin.
@Tag(name = "Usuarios", description = "Gestion de usuarios del sistema RedNorte")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;
    private final RecuperacionService recuperacionService;

    public UsuarioController(UsuarioService usuarioService, JwtUtil jwtUtil, RecuperacionService recuperacionService) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
        this.recuperacionService = recuperacionService;
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
        @ApiResponse(responseCode = "400", description = "Datos invalidos")
    })
    @PostMapping
    public void usuarioAlmacenar(@RequestBody Usuario usuario) {
        this.usuarioService.usuarioAlmacenar(usuario);
    }

    @Operation(summary = "Login de usuario", description = "Autentica con mail y contrasena. Retorna datos del usuario y un token JWT si las credenciales son validas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login exitoso"),
        @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDTO> login(@RequestBody UsuarioRequestDTO u) {
        Usuario usuario = this.usuarioService.login(u.getMail(), u.getPass());
        if (usuario == null) return ResponseEntity.status(401).build();

        UsuarioResponseDTO dto = toDTO(usuario);
        String rolTag = usuario.getRol() != null ? usuario.getRol().getTag() : "PACIENTE";
        String token = jwtUtil.generarToken(usuario.getMail(), rolTag, usuario.getId());
        dto.setToken(token);

        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Solicitar codigo de recuperacion de contraseña",
            description = "Envia un codigo de 6 digitos al correo si la cuenta existe. Por seguridad, siempre responde 200 sin revelar si el mail esta registrado o no.")
    @PostMapping("/recuperacion/solicitar")
    public ResponseEntity<Map<String, String>> solicitarRecuperacion(@RequestBody Map<String, String> body) {
        String mail = body.get("mail");
        recuperacionService.solicitarCodigo(mail);
        return ResponseEntity.ok(Map.of(
            "mensaje", "Si el correo está registrado, recibirás un código de verificación en unos minutos."
        ));
    }

    @Operation(summary = "Validar el codigo de recuperacion",
            description = "Confirma si el codigo de 6 digitos es correcto y no ha expirado, antes de pedir la nueva contraseña.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Codigo valido"),
        @ApiResponse(responseCode = "400", description = "Codigo invalido o expirado")
    })
    @PostMapping("/recuperacion/validar")
    public ResponseEntity<Map<String, String>> validarCodigoRecuperacion(@RequestBody Map<String, String> body) {
        boolean valido = recuperacionService.validarCodigo(body.get("mail"), body.get("codigo"));
        if (!valido) {
            return ResponseEntity.badRequest().body(Map.of("error", "El código ingresado no es válido o ya expiró."));
        }
        return ResponseEntity.ok(Map.of("mensaje", "Código válido."));
    }

    @Operation(summary = "Cambiar la contraseña usando el codigo de recuperacion",
            description = "Valida el codigo nuevamente (defensa en profundidad) y, si es correcto, actualiza la contraseña.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contraseña actualizada"),
        @ApiResponse(responseCode = "400", description = "Codigo invalido o expirado")
    })
    @PostMapping("/recuperacion/cambiar")
    public ResponseEntity<Map<String, String>> cambiarPasswordConCodigo(@RequestBody Map<String, String> body) {
        boolean exito = recuperacionService.cambiarPassword(
                body.get("mail"), body.get("codigo"), body.get("nuevaPassword"));
        if (!exito) {
            return ResponseEntity.badRequest().body(Map.of("error", "El código ingresado no es válido o ya expiró."));
        }
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada correctamente."));
    }
}
