package com.example.Backend_usuarios.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Envio de correos reales via SMTP (Gmail), con plantilla HTML propia
 * de marca (logo embebido, colores de RedNorte, banner de cierre).
 * Dos usos en el sistema:
 *
 *  1) Codigo de recuperacion de contraseña (6 digitos, 15 min de validez).
 *  2) Confirmacion de cita agendada (la dispara ms-chatbot o
 *     AgendarPage.jsx justo despues de reservar el bloque con exito).
 *
 * El logo se embebe con Content-ID (CID) en vez de referenciarlo como
 * URL externa: asi se ve igual en cualquier cliente de correo, sin
 * depender de que el sitio este publicado en una URL accesible desde
 * internet (localhost no sirve para esto).
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
    private static final String LOGO_CID = "logoRedNorte";

    private final JavaMailSender mailSender;
    private final String remitente;

    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    public void enviarCodigoRecuperacion(String destinatario, String codigo) {
        String asunto = "RedNorte — Código para recuperar tu contraseña";
        String cuerpoHtml = plantillaBase(
                "Recuperación de contraseña",
                "Recibimos una solicitud para restablecer la contraseña de tu cuenta en RedNorte.",
                "<div style=\"text-align:center; margin: 28px 0;\">" +
                "<div style=\"display:inline-block; background:#EFF6FF; border:2px dashed #2563EB; " +
                "border-radius:12px; padding:18px 36px;\">" +
                "<span style=\"font-family:'Courier New',monospace; font-size:34px; font-weight:800; " +
                "letter-spacing:8px; color:#1E3A8A;\">" + codigo + "</span>" +
                "</div></div>" +
                "<p style=\"text-align:center; color:#64748B; font-size:13.5px;\">" +
                "Este código es válido por <strong>15 minutos</strong>.</p>",
                "Si no solicitaste este cambio, puedes ignorar este correo de forma segura: tu contraseña actual seguirá funcionando."
        );
        enviarHtml(destinatario, asunto, cuerpoHtml);
    }

    public void enviarConfirmacionCita(String destinatario, String nombrePaciente, String especialidad,
                                        String fecha, String hora) {
        String asunto = "RedNorte — Tu cita médica fue confirmada";
        String filas =
                filaDetalle("👤", "Paciente", nombrePaciente) +
                filaDetalle("🩺", "Especialidad", especialidad) +
                filaDetalle("📅", "Fecha", fecha) +
                filaDetalle("🕐", "Hora", hora);
        String cuerpoHtml = plantillaBase(
                "¡Tu cita fue agendada con éxito!",
                "Hola " + nombrePaciente + ", confirmamos los detalles de tu próxima atención médica.",
                "<div style=\"background:#F0FDF4; border:1px solid #BBF7D0; border-radius:12px; " +
                "padding:20px 24px; margin: 24px 0;\">" + filas + "</div>",
                "Si necesitas reprogramar o cancelar tu cita, puedes hacerlo desde tu cuenta en RedNorte, sección \"Mis Consultas\"."
        );
        enviarHtml(destinatario, asunto, cuerpoHtml);
    }

    private String filaDetalle(String emoji, String etiqueta, String valor) {
        return "<div style=\"display:flex; justify-content:space-between; padding:8px 0; " +
                "border-bottom:1px solid #DCFCE7; font-size:14px;\">" +
                "<span style=\"color:#166534;\">" + emoji + " " + etiqueta + "</span>" +
                "<strong style=\"color:#14532D;\">" + valor + "</strong>" +
                "</div>";
    }

    /**
     * Plantilla HTML compartida: header con logo en gradiente de marca,
     * cuerpo con el contenido especifico de cada correo, y un banner de
     * cierre con los datos de contacto de la clinica.
     */
    private String plantillaBase(String titulo, String introduccion, String contenidoCentral, String notaFinal) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <body style="margin:0; padding:0; background:#F1F5F9; font-family:'Segoe UI',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#F1F5F9; padding:32px 0;">
                    <tr><td align="center">
                      <table width="560" cellpadding="0" cellspacing="0" style="background:#FFFFFF; border-radius:16px; overflow:hidden; box-shadow:0 4px 24px rgba(15,23,42,0.08);">

                        <tr><td style="background:linear-gradient(135deg,#1E3A8A,#0D9488); padding:32px 24px; text-align:center;">
                          <img src="cid:%s" alt="RedNorte" width="120" style="display:block; margin:0 auto 12px;" />
                          <div style="color:#FFFFFF; font-size:20px; font-weight:800;">%s</div>
                        </td></tr>

                        <tr><td style="padding:32px 36px 8px;">
                          <p style="color:#334155; font-size:15px; line-height:1.6; margin:0 0 4px;">%s</p>
                          %s
                        </td></tr>

                        <tr><td style="padding:0 36px 28px;">
                          <p style="color:#94A3B8; font-size:12.5px; line-height:1.6; margin:0;">%s</p>
                        </td></tr>

                        <tr><td style="background:#0F172A; padding:24px 36px; text-align:center;">
                          <div style="color:#5EEAD4; font-weight:800; font-size:15px; margin-bottom:6px;">RedNorte — Clínica Digital</div>
                          <div style="color:#94A3B8; font-size:12px; line-height:1.6;">
                            Atención médica accesible para el norte de Chile<br/>
                            📧 rednorte.in@gmail.com &nbsp;·&nbsp; 🌐 rednorte.cl
                          </div>
                          <div style="color:#475569; font-size:11px; margin-top:14px;">
                            Este es un correo automático, por favor no respondas a esta dirección.
                          </div>
                        </td></tr>

                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(LOGO_CID, titulo, introduccion, contenidoCentral, notaFinal);
    }

    private void enviarHtml(String destinatario, String asunto, String cuerpoHtml) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(cuerpoHtml, true);
            helper.addInline(LOGO_CID, new ClassPathResource("logo-email.png"));
            mailSender.send(mimeMessage);
            log.info("Correo enviado correctamente a {}", destinatario);
        } catch (MessagingException e) {
            log.error("No se pudo enviar el correo a {}: {}", destinatario, e.getMessage());
        }
    }
}
