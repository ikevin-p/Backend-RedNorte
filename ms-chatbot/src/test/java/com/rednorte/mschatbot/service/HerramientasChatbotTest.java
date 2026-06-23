package com.rednorte.mschatbot.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios de HerramientasChatbot.
 *
 * Ya no hay llamadas HTTP a otros microservicios que mockear (ver el
 * javadoc de la clase): la unica herramienta real es iniciar_flujo_ui,
 * que solo valida el tipo de formulario solicitado y devuelve un JSON
 * de confirmacion para que ChatbotService lo traduzca en una accion
 * de UI (REDIRIGIR_REGISTRO/LOGIN/RECUPERAR/AGENDAR).
 */
@DisplayName("Tests unitarios — HerramientasChatbot")
class HerramientasChatbotTest {

    private final HerramientasChatbot herramientas = new HerramientasChatbot();

    @Test
    @DisplayName("definiciones() expone unicamente la herramienta iniciar_flujo_ui")
    void definiciones_expoeSoloIniciarFlujoUi() {
        var definiciones = herramientas.definiciones();
        assertEquals(1, definiciones.size());
        assertEquals("iniciar_flujo_ui", definiciones.get(0).getFunction().getName());
    }

    @Test
    @DisplayName("iniciar_flujo_ui con tipo REGISTRO confirma el formulario de registro")
    void ejecutar_iniciarFlujoUiRegistro_confirmaFormulario() {
        String resultado = herramientas.ejecutar("iniciar_flujo_ui", Map.of("tipo", "REGISTRO"), null, null).block();
        assertEquals("{\"exito\": true, \"tipoFormulario\": \"REGISTRO\"}", resultado);
    }

    @Test
    @DisplayName("iniciar_flujo_ui con tipo LOGIN confirma el formulario de inicio de sesion")
    void ejecutar_iniciarFlujoUiLogin_confirmaFormulario() {
        String resultado = herramientas.ejecutar("iniciar_flujo_ui", Map.of("tipo", "login"), "USR010", "Juan Perez").block();
        assertEquals("{\"exito\": true, \"tipoFormulario\": \"LOGIN\"}", resultado);
    }

    @Test
    @DisplayName("iniciar_flujo_ui con tipo RECUPERAR confirma el formulario de recuperacion")
    void ejecutar_iniciarFlujoUiRecuperar_confirmaFormulario() {
        String resultado = herramientas.ejecutar("iniciar_flujo_ui", Map.of("tipo", "RECUPERAR"), null, null).block();
        assertEquals("{\"exito\": true, \"tipoFormulario\": \"RECUPERAR\"}", resultado);
    }

    @Test
    @DisplayName("iniciar_flujo_ui con tipo AGENDAR confirma el modal de agendamiento")
    void ejecutar_iniciarFlujoUiAgendar_confirmaFormulario() {
        String resultado = herramientas.ejecutar("iniciar_flujo_ui", Map.of("tipo", "AGENDAR"), "USR010", "Juan Perez").block();
        assertEquals("{\"exito\": true, \"tipoFormulario\": \"AGENDAR\"}", resultado);
    }

    @Test
    @DisplayName("iniciar_flujo_ui sin tipo (o con uno invalido) usa REGISTRO como respaldo")
    void ejecutar_iniciarFlujoUiSinTipo_usaRegistroPorDefecto() {
        String resultado = herramientas.ejecutar("iniciar_flujo_ui", Map.of(), null, null).block();
        assertEquals("{\"exito\": true, \"tipoFormulario\": \"REGISTRO\"}", resultado);

        String resultadoInvalido = herramientas.ejecutar("iniciar_flujo_ui", Map.of("tipo", "ALGO_INVENTADO"), null, null).block();
        assertEquals("{\"exito\": true, \"tipoFormulario\": \"REGISTRO\"}", resultadoInvalido);
    }

    @Test
    @DisplayName("Una herramienta inexistente retorna un JSON de error con exito=false")
    void ejecutar_herramientaDesconocida_retornaError() {
        String resultado = herramientas.ejecutar("agendar_cita_por_texto", Map.of(), "USR010", "Juan Perez").block();
        assertTrue(resultado.contains("\"exito\": false"));
        assertTrue(resultado.contains("Herramienta desconocida"));
    }
}
