package com.rednorte.msconsultas;

/**
 * DESACTIVADO: el CORS lo maneja exclusivamente el API Gateway
 * (globalcors en application.yml de api-gateway).
 *
 * Si este WebMvcConfigurer tambien agrega headers Access-Control-Allow-*,
 * el navegador los recibe duplicados (uno del Gateway, otro de aqui) y
 * bloquea la respuesta con el error:
 * "The 'Access-Control-Allow-Origin' header contains multiple values"
 *
 * Se deja la clase sin @Configuration para que Spring no la registre,
 * en vez de borrar el archivo, por si se necesita reactivar en el futuro
 * (por ejemplo si este microservicio alguna vez se expone sin Gateway).
 */
public class CorsConfig {
    // Bean deshabilitado intencionalmente.
}
