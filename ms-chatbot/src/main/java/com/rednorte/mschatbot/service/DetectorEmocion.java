package com.rednorte.mschatbot.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Decide que emocion debe mostrar el avatar de SaludBot junto a una
 * respuesta, mediante reglas simples sobre texto y estado de la
 * conversacion (no se le pide al modelo que clasifique su propio tono:
 * mas lento e impredecible que aplicar reglas aqui).
 *
 * Precedencia (de mayor a menor prioridad) cuando varias reglas
 * podrian aplicar al mismo turno:
 *   1. CELEBRACION  - la cita se agendo de verdad
 *   2. ALERTA       - la respuesta deriva a Urgencias
 *   3. CONFUNDIDO   - error real detectado (RUT invalido, falla de herramienta)
 *   4. EMPATICO     - el paciente describe dolor/molestia (sin derivar a Urgencias)
 *   5. DESPEDIDA    - el paciente se esta despidiendo
 *   6. ACOGEDOR     - es el primer mensaje de la conversacion
 *   (CONCENTRADO no se decide aqui: lo muestra el frontend mientras espera la respuesta)
 */
@Component
public class DetectorEmocion {

    public static final String ACOGEDOR = "ACOGEDOR";
    public static final String CELEBRACION = "CELEBRACION";
    public static final String CONFUNDIDO = "CONFUNDIDO";
    public static final String EMPATICO = "EMPATICO";
    public static final String ALERTA = "ALERTA";
    public static final String DESPEDIDA = "DESPEDIDA";
    public static final String NEUTRAL = "NEUTRAL";

    private static final List<String> PALABRAS_URGENCIA = List.of(
            "urgencia", "urgencias", "131", "dolor de pecho", "dificultad para respirar",
            "no puedo respirar", "sangrado severo", "perdida de conciencia", "perdió la conciencia"
    );

    private static final List<String> PALABRAS_DOLOR = List.of(
            "dolor", "duele", "molestia", "me siento mal", "me duele", "fiebre",
            "preocupad", "asustad", "frustrad", "angustia"
    );

    private static final List<String> PALABRAS_DESPEDIDA = List.of(
            "gracias", "muchas gracias", "chao", "adios", "adiós", "hasta luego", "nos vemos", "bye"
    );

    /**
     * @param mensajeUsuario      texto que escribio el paciente en este turno
     * @param respuestaAsistente  texto final que el modelo redacto
     * @param esPrimerTurno       true si historialGuardado estaba vacio antes de este mensaje
     * @param huboErrorHerramienta true si alguna herramienta devolvio exito=false en este turno
     */
    public String detectar(String mensajeUsuario, String respuestaAsistente,
                            boolean esPrimerTurno, boolean huboErrorHerramienta) {

        String msgLower = normalizar(mensajeUsuario);
        String respLower = normalizar(respuestaAsistente);

        if (contieneAlguna(respLower, PALABRAS_URGENCIA)) {
            return ALERTA;
        }
        if (huboErrorHerramienta || esRutInvalidoMencionado(respLower)) {
            return CONFUNDIDO;
        }
        if (contieneAlguna(msgLower, PALABRAS_DOLOR)) {
            return EMPATICO;
        }
        if (contieneAlguna(msgLower, PALABRAS_DESPEDIDA)) {
            return DESPEDIDA;
        }
        if (esPrimerTurno) {
            return ACOGEDOR;
        }
        return NEUTRAL;
    }

    /** Sobrescribe cualquier deteccion anterior: una cita agendada con exito siempre es Celebracion. */
    public String forzarCelebracion() {
        return CELEBRACION;
    }

    private static final List<String> FRASES_INVALIDEZ = List.of(
            "inválido", "invalido", "no válido", "no valido", "no es válido", "no es valido",
            "no es correcto", "incorrecto", "no corresponde", "está mal", "esta mal"
    );

    private boolean esRutInvalidoMencionado(String textoLower) {
        return textoLower.contains("rut") && contieneAlguna(textoLower, FRASES_INVALIDEZ);
    }

    private boolean contieneAlguna(String texto, List<String> palabras) {
        if (texto == null) return false;
        return palabras.stream().anyMatch(texto::contains);
    }

    private String normalizar(String texto) {
        return texto == null ? "" : texto.toLowerCase(Locale.forLanguageTag("es"));
    }
}
