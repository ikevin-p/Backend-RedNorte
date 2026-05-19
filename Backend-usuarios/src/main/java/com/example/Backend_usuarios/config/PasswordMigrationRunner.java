package com.example.Backend_usuarios.config;

import com.example.Backend_usuarios.model.Usuario;
import com.example.Backend_usuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Migrador de contraseñas — se ejecuta UNA vez al arrancar la app.
 * Detecta contraseñas en texto plano (no empiezan con $2a$)
 * y las reemplaza por su hash BCrypt.
 * Una vez migradas, no vuelve a tocarlas.
 */
@Component
public class PasswordMigrationRunner implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigrationRunner(UsuarioRepository usuarioRepository,
                                   PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder   = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        int migrados = 0;

        for (Usuario u : usuarios) {
            String pass = u.getPass();

            // Si ya es un hash BCrypt, no hacer nada
            if (pass != null && pass.startsWith("$2a$")) continue;

            // Hashear y guardar
            u.setPass(passwordEncoder.encode(pass));
            usuarioRepository.save(u);
            migrados++;
            System.out.println("[BCrypt] Contrasena migrada: " + u.getMail());
        }

        if (migrados > 0) {
            System.out.println("[BCrypt] Migracion completada: " + migrados + " usuario(s) actualizados.");
        } else {
            System.out.println("[BCrypt] Todas las contrasenas ya estan hasheadas. OK.");
        }
    }
}
