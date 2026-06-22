package com.example.Backend_usuarios.service;

import com.example.Backend_usuarios.model.CodigoRecuperacion;
import com.example.Backend_usuarios.model.Usuario;
import com.example.Backend_usuarios.repository.CodigoRecuperacionRepository;
import com.example.Backend_usuarios.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Flujo completo de "olvidé mi contraseña":
 *
 *  1) solicitarCodigo: genera un codigo de 6 digitos, lo guarda con
 *     expiracion de 15 min, y dispara el correo. Por seguridad, NO
 *     revela si el mail existe o no en el sistema (siempre responde
 *     igual hacia afuera, para no permitir que alguien use este
 *     endpoint para averiguar que correos estan registrados).
 *  2) validarCodigo: confirma que el codigo es correcto y no expiro,
 *     sin marcarlo como usado todavia (se usa para la pantalla
 *     intermedia "código correcto, ahora ingresa tu nueva contraseña").
 *  3) cambiarPassword: vuelve a validar el codigo (defensa en
 *     profundidad: nunca confiar en que el frontend ya lo valido antes)
 *     y, si es correcto, actualiza la contraseña y marca el codigo
 *     como usado para que no se pueda reutilizar.
 */
@Service
public class RecuperacionService {

    private static final int DURACION_CODIGO_MINUTOS = 15;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final CodigoRecuperacionRepository codigoRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public RecuperacionService(UsuarioRepository usuarioRepository,
                                CodigoRecuperacionRepository codigoRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.codigoRepository = codigoRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public void solicitarCodigo(String mail) {
        Optional<Usuario> usuario = usuarioRepository.findByMail(mail);
        if (usuario.isEmpty()) {
            // No se revela que el mail no existe: se responde igual
            // hacia afuera (ver el controller), solo que aqui no se
            // genera ni envia nada de verdad.
            return;
        }

        String codigo = generarCodigoDeSeisDigitos();

        CodigoRecuperacion entidad = new CodigoRecuperacion();
        entidad.setMail(mail);
        entidad.setCodigo(codigo);
        entidad.setExpira(LocalDateTime.now().plusMinutes(DURACION_CODIGO_MINUTOS));
        codigoRepository.save(entidad);

        emailService.enviarCodigoRecuperacion(mail, codigo);
    }

    public boolean validarCodigo(String mail, String codigo) {
        return codigoRepository.findTopByMailAndCodigoOrderByCreadoDesc(mail, codigo)
                .map(CodigoRecuperacion::esValido)
                .orElse(false);
    }

    public boolean cambiarPassword(String mail, String codigo, String nuevaPassword) {
        Optional<CodigoRecuperacion> codigoOpt = codigoRepository
                .findTopByMailAndCodigoOrderByCreadoDesc(mail, codigo);

        if (codigoOpt.isEmpty() || !codigoOpt.get().esValido()) {
            return false;
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByMail(mail);
        if (usuarioOpt.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setPass(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        CodigoRecuperacion codigoEntidad = codigoOpt.get();
        codigoEntidad.setUsado(true);
        codigoRepository.save(codigoEntidad);

        return true;
    }

    private String generarCodigoDeSeisDigitos() {
        int numero = RANDOM.nextInt(1_000_000); // 0 a 999999
        return String.format("%06d", numero);
    }
}
