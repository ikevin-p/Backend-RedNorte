package com.rednorte.mschatbot.service;

import com.rednorte.mschatbot.dto.ollama.OllamaTool;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Implementacion de las herramientas que SaludBot puede invocar.
 *
 * SaludBot ya NO orquesta acciones de negocio por su cuenta (agendar
 * una cita, crear una consulta, reservar un bloque): la unica
 * herramienta real es iniciar_flujo_ui, que solo confirma la intencion
 * detectada y le indica al frontend (ChatbotWidget.jsx) que formulario
 * visual abrir. El paciente completa esa accion en un componente de UI
 * normal (los mismos de RegistroPage/LoginPage/RecuperarPasswordModal/
 * AgendarPage, reutilizados como modal), autenticado con su propia
 * sesion del navegador. Esto reemplaza el enfoque anterior en el que
 * el bot llamaba directamente a ms-agenda-medica/ms-consultas/
 * ms-usuarios para agendar citas via texto: mas simple, sin duplicar
 * la logica de reserva, y sin que el chatbot necesite credenciales
 * propias para hablar con otros microservicios.
 */
@Service
public class HerramientasChatbot {

    /** Las definiciones que se le envian a Ollama en cada turno. */
    public List<OllamaTool> definiciones() {
        return List.of(
                new OllamaTool(new OllamaTool.Funcion(
                        "iniciar_flujo_ui",
                        "Abre un formulario o modal en la interfaz para que el paciente complete una accion por su " +
                        "cuenta (NUNCA se le pide la contraseña, RUT, fecha u hora de una cita por chat). Usar esta " +
                        "herramienta INMEDIATAMENTE cuando el paciente pida ayuda para registrarse, crear una cuenta, " +
                        "iniciar sesion, recuperar u olvido su contraseña, o cuando confirme que quiere agendar una " +
                        "cita medica (si no tiene cuenta, usa tipo=REGISTRO primero; si ya tiene cuenta activa, usa " +
                        "tipo=AGENDAR directamente, sin pedirle fecha, hora, especialidad ni RUT por el chat: todo " +
                        "eso lo completa visualmente en el formulario que se abre). No existe ninguna otra forma de " +
                        "crear cuentas, iniciar sesion, recuperar contraseñas o agendar citas: nunca inventes otra " +
                        "herramienta para esto, y nunca le digas al paciente que inicie sesion para poder recuperar " +
                        "su contraseña (eso no tiene sentido: usa tipo=RECUPERAR directamente).",
                        Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "tipo", Map.of(
                                                "type", "string",
                                                "enum", List.of("REGISTRO", "LOGIN", "RECUPERAR", "AGENDAR"),
                                                "description", "REGISTRO si quiere crear una cuenta nueva, LOGIN si ya tiene cuenta pero no puede ingresar, " +
                                                        "RECUPERAR si olvido su contraseña, AGENDAR si quiere reservar una cita medica y ya tiene cuenta activa"
                                        )
                                ),
                                "required", List.of("tipo")
                        )
                ))
        );
    }

    /** Despacha la herramienta solicitada por el modelo y devuelve el resultado como JSON string. */
    public Mono<String> ejecutar(String nombreHerramienta, Map<String, Object> argumentos,
                                  String usuarioId, String nombrePacienteSesion) {
        return switch (nombreHerramienta) {
            case "iniciar_flujo_ui" -> iniciarFlujoUi(argumentos);
            default -> Mono.just(error("Herramienta desconocida: " + nombreHerramienta));
        };
    }

    /**
     * No llama a ningun microservicio: solo confirma la intencion para
     * que ChatbotService la traduzca en una accion que el frontend
     * interprete (que modal abrir). El paciente completa la accion en
     * ese formulario, nunca dentro del chat.
     */
    private Mono<String> iniciarFlujoUi(Map<String, Object> argumentos) {
        String tipo = String.valueOf(argumentos.getOrDefault("tipo", "REGISTRO")).toUpperCase();
        if (!tipo.equals("REGISTRO") && !tipo.equals("LOGIN") && !tipo.equals("RECUPERAR") && !tipo.equals("AGENDAR")) {
            tipo = "REGISTRO";
        }
        return Mono.just("{\"exito\": true, \"tipoFormulario\": \"" + tipo + "\"}");
    }

    private String error(String mensaje) {
        return "{\"exito\": false, \"error\": \"" + mensaje.replace("\"", "'") + "\"}";
    }
}
