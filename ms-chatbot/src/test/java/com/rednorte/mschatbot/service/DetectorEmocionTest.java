package com.rednorte.mschatbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Tests unitarios — DetectorEmocion")
class DetectorEmocionTest {

    private DetectorEmocion detector;

    @BeforeEach
    void setUp() {
        detector = new DetectorEmocion();
    }

    @Test
    @DisplayName("Primer turno sin otra senal mas fuerte retorna ACOGEDOR")
    void detectar_primerTurnoSinOtraSenal_retornaAcogedor() {
        String emocion = detector.detectar("Hola, necesito ayuda", "¡Hola! Bienvenido a RedNorte", true, false);
        assertEquals(DetectorEmocion.ACOGEDOR, emocion);
    }

    @Test
    @DisplayName("Turno posterior sin ninguna senal especial retorna NEUTRAL")
    void detectar_turnoPosteriorSinSenal_retornaNeutral() {
        String emocion = detector.detectar("Quiero saber el horario", "El horario de atención es...", false, false);
        assertEquals(DetectorEmocion.NEUTRAL, emocion);
    }

    @Test
    @DisplayName("Respuesta que menciona Urgencias retorna ALERTA, sin importar el resto")
    void detectar_respuestaConUrgencias_retornaAlerta() {
        String emocion = detector.detectar(
                "tengo mucho dolor de pecho",
                "Esto es importante: acude a Urgencias de inmediato o llama al 131.",
                false, false);
        assertEquals(DetectorEmocion.ALERTA, emocion);
    }

    @Test
    @DisplayName("ALERTA tiene prioridad incluso en el primer turno")
    void detectar_alertaEnPrimerTurno_priorizaAlertaSobreAcogedor() {
        String emocion = detector.detectar(
                "no puedo respirar bien",
                "Por favor acude a Urgencias de inmediato.",
                true, false);
        assertEquals(DetectorEmocion.ALERTA, emocion);
    }

    @Test
    @DisplayName("Mensaje con dolor/molestia, sin Urgencias, retorna EMPATICO")
    void detectar_mensajeConDolorSinUrgencia_retornaEmpatico() {
        String emocion = detector.detectar(
                "me duele bastante la cabeza desde ayer",
                "Lamento que te sientas así. Cuéntame más para ayudarte a agendar.",
                false, false);
        assertEquals(DetectorEmocion.EMPATICO, emocion);
    }

    @Test
    @DisplayName("Si hubo un error de herramienta, retorna CONFUNDIDO incluso si el mensaje no lo sugiere")
    void detectar_huboErrorHerramienta_retornaConfundido() {
        String emocion = detector.detectar(
                "quiero agendar para el lunes a las 8",
                "Hubo un problema al agendar tu cita, ¿puedes intentar con otro horario?",
                false, true);
        assertEquals(DetectorEmocion.CONFUNDIDO, emocion);
    }

    @Test
    @DisplayName("CONFUNDIDO tiene prioridad sobre EMPATICO cuando ambas senales aplican")
    void detectar_errorYDolorJuntos_priorizaConfundido() {
        String emocion = detector.detectar(
                "me duele la cabeza y quiero agendar",
                "Tuvimos un error al procesar tu solicitud.",
                false, true);
        assertEquals(DetectorEmocion.CONFUNDIDO, emocion);
    }

    @Test
    @DisplayName("Respuesta que menciona un RUT invalido retorna CONFUNDIDO")
    void detectar_rutInvalidoMencionado_retornaConfundido() {
        String emocion = detector.detectar(
                "mi rut es 12345",
                "Ese RUT no es válido, ¿puedes verificarlo?",
                false, false);
        assertEquals(DetectorEmocion.CONFUNDIDO, emocion);
    }

    @Test
    @DisplayName("Mensaje de despedida retorna DESPEDIDA")
    void detectar_mensajeDeDespedida_retornaDespedida() {
        String emocion = detector.detectar("muchas gracias por tu ayuda, chao", "¡De nada! Que tengas un buen día.", false, false);
        assertEquals(DetectorEmocion.DESPEDIDA, emocion);
    }

    @Test
    @DisplayName("ALERTA tiene prioridad sobre DESPEDIDA si ambas aplican")
    void detectar_alertaYDespedidaJuntas_priorizaAlerta() {
        String emocion = detector.detectar(
                "gracias, pero tengo mucho dolor de pecho",
                "Antes de despedirnos: por favor acude a Urgencias ahora mismo.",
                false, false);
        assertEquals(DetectorEmocion.ALERTA, emocion);
    }

    @Test
    @DisplayName("forzarCelebracion siempre retorna CELEBRACION")
    void forzarCelebracion_retornaCelebracion() {
        assertEquals(DetectorEmocion.CELEBRACION, detector.forzarCelebracion());
    }

    @Test
    @DisplayName("Maneja mensaje null sin lanzar excepcion, retornando ACOGEDOR si es primer turno")
    void detectar_mensajeNull_noLanzaExcepcionYRetornaAcogedor() {
        String emocion = detector.detectar(null, null, true, false);
        assertEquals(DetectorEmocion.ACOGEDOR, emocion);
    }
}
