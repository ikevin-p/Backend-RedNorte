package com.example.Backend_usuarios.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envio de correos reales via SMTP (Gmail). Dos usos en el sistema:
 *
 *  1) Codigo de recuperacion de contraseña (6 digitos, 15 min de validez).
 *  2) Confirmacion de cita agendada (la dispara ms-chatbot o
 *     AgendarPage.jsx justo despues de reservar el bloque con exito).
 *
 * Si el envio falla (SMTP caido, credenciales malas, etc.) NO se
 * relanza la excepcion hacia el controller: el flujo de negocio (crear
 * la cuenta, agendar la cita) ya se completo correctamente en la base
 * de datos antes de llegar aqui, y no tiene sentido que un correo que
 * no salio bloquee algo que ya funciono. Se deja registrado en el log
 * para poder diagnosticarlo despues.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String remitente;

    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    public void enviarCodigoRecuperacion(String destinatario, String codigo) {
        String asunto = "RedNorte — Código para recuperar tu contraseña";
        String cuerpo = """
                Hola,

                Recibimos una solicitud para restablecer la contraseña de tu cuenta en RedNorte.

                Tu código de verificación es:

                        %s

                Este código es válido por 15 minutos. Si no solicitaste este cambio, puedes ignorar este correo.

                — Equipo RedNorte
                """.formatted(codigo);
        enviar(destinatario, asunto, cuerpo);
    }

    public void enviarConfirmacionCita(String destinatario, String nombrePaciente, String especialidad,
                                        String fecha, String hora) {
        String asunto = "RedNorte — Tu cita médica fue confirmada";
        String cuerpo = """
                Hola %s,

                Tu cita médica ha sido agendada con éxito. Aquí tienes los detalles:

                        Especialidad: %s
                        Fecha:        %s
                        Hora:         %s

                Te esperamos. Si necesitas reprogramar o cancelar, puedes hacerlo desde tu cuenta en RedNorte.

                — Equipo RedNorte
                """.formatted(nombrePaciente, especialidad, fecha, hora);
        enviar(destinatario, asunto, cuerpo);
    }

    private void enviar(String destinatario, String asunto, String cuerpo) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            mailSender.send(mensaje);
            log.info("Correo enviado correctamente a {}", destinatario);
        } catch (Exception e) {
            log.error("No se pudo enviar el correo a {}: {}", destinatario, e.getMessage());
        }
    }
}
