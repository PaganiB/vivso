package com.vivso.Vivso.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
public class EmailService implements  IEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Value("${vivso.app.url:http://localhost:5173}")
    private String appUrl;

    @Override
    public void enviarCredencialesYAprobacion(String email, String username, String passwordTemporal, String nombreOrganizacion) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom);
            helper.setTo(email);
            helper.setSubject("¡Bienvenido a VIVSO! - Tu organización ha sido aprobada");

            String htmlContent = construirHtmlCredencialesYAprobacion(username, passwordTemporal, nombreOrganizacion);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email de credenciales y aprobación enviado a: {}", email);

        } catch (MessagingException e) {
            log.error("Error al enviar email a {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("No se pudo enviar el email", e);
        }
    }

    /**
     * Construye el HTML con credenciales + notificación de aprobación
     */
    private String construirHtmlCredencialesYAprobacion(String username, String password, String nombreOrganizacion) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            line-height: 1.6;
                            color: #333;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                            background-color: #f5f5f5;
                        }
                        .header {
                            background-color: #27ae60;
                            color: white;
                            padding: 30px;
                            text-align: center;
                            border-radius: 5px 5px 0 0;
                        }
                        .content {
                            background-color: white;
                            padding: 30px;
                            border-radius: 0 0 5px 5px;
                        }
                        .org-box {
                            background-color: #ecf7ec;
                            padding: 15px;
                            border-left: 4px solid #27ae60;
                            margin: 20px 0;
                            border-radius: 3px;
                        }
                        .credentials-box {
                            background-color: #ecf0f1;
                            padding: 20px;
                            border-left: 4px solid #3498db;
                            margin: 20px 0;
                            border-radius: 3px;
                        }
                        .label {
                            font-weight: bold;
                            color: #2c3e50;
                            margin-top: 10px;
                        }
                        .value {
                            background-color: #fff;
                            padding: 8px 12px;
                            margin-top: 5px;
                            border-radius: 3px;
                            font-family: monospace;
                            border: 1px solid #bdc3c7;
                        }
                        .warning {
                            background-color: #fff3cd;
                            padding: 15px;
                            border-left: 4px solid #ffc107;
                            margin: 20px 0;
                            border-radius: 3px;
                            color: #856404;
                        }
                        .button {
                            display: inline-block;
                            background-color: #27ae60;
                            color: white;
                            padding: 12px 30px;
                            text-decoration: none;
                            border-radius: 5px;
                            margin-top: 20px;
                        }
                        .footer {
                            font-size: 12px;
                            color: #7f8c8d;
                            margin-top: 20px;
                            padding-top: 20px;
                            border-top: 1px solid #ecf0f1;
                            text-align: center;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>✓ ¡Solicitud Aprobada!</h1>
                            <p>Tu organización ha sido activada en VIVSO</p>
                        </div>
                        <div class="content">
                            <p>Hola,</p>
                
                            <div class="org-box">
                                <p><strong>Organización aprobada:</strong> """ + nombreOrganizacion + """
                </p>
                                        <p>Tu solicitud ha sido verificada y aprobada. Ya podés acceder a VIVSO con las credenciales de abajo.</p>
                                    </div>
                
                                    <p><strong>Tus credenciales de acceso:</strong></p>
                                    <div class="credentials-box">
                                        <div class="label">Usuario:</div>
                                        <div class="value">""" + username + """
                </div>
                
                                        <div class="label">Contraseña temporal:</div>
                                        <div class="value">""" + password + """
                </div>
                                    </div>
                
                                    <div class="warning">
                                        <strong>⚠️ Importante:</strong> Esta es una contraseña temporal. <strong>Deberás cambiarla en tu primer acceso</strong> por razones de seguridad.
                                    </div>
                
                                    <p><strong>Pasos para acceder:</strong></p>
                                    <ol>
                                        <li>Ingresá a <a href=\"""" + appUrl + """
                \" target="_blank">""" + appUrl + """
                    </a></li>
                    <li>Usá el usuario y contraseña temporal de arriba</li>
                    <li>Cambiar tu contraseña cuando accedas</li>
                </ol>
                
                <center>
                    <a href=\"""" + appUrl + """
                            \" class="button">Acceder a VIVSO</a>
                            </center>
                
                            <div class="footer">
                                <p>Si tenés problemas para acceder, contactá al administrador.</p>
                                <p>&copy; 2026 VIVSO - Todos los derechos reservados</p>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    @Override
    public void enviarNotificacionSolicitudRechazada(String email, String nombreOrganizacion, String motivo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom);
            helper.setTo(email);
            helper.setSubject("Solicitud Rechazada - VIVSO");

            String htmlContent = construirHtmlSolicitudRechazada(nombreOrganizacion, motivo);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email de rechazo enviado a: {}", email);

        } catch (MessagingException e) {
            log.error("Error al enviar email de rechazo a {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("No se pudo enviar el email de rechazo", e);
        }
    }

    private String construirHtmlSolicitudRechazada(String nombreOrganizacion, String motivo) {
        return """
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    line-height: 1.6;
                    color: #333;
                }
                .container {
                    max-width: 600px;
                    margin: 0 auto;
                    padding: 20px;
                    background-color: #f5f5f5;
                }
                .header {
                    background-color: #e74c3c;
                    color: white;
                    padding: 30px;
                    text-align: center;
                    border-radius: 5px 5px 0 0;
                }
                .content {
                    background-color: white;
                    padding: 30px;
                    border-radius: 0 0 5px 5px;
                }
                .motivo-box {
                    background-color: #fadbd8;
                    padding: 20px;
                    border-left: 4px solid #e74c3c;
                    margin: 20px 0;
                    border-radius: 3px;
                }
                .label {
                    font-weight: bold;
                    color: #2c3e50;
                    margin-top: 10px;
                }
                .value {
                    background-color: #fff;
                    padding: 8px 12px;
                    margin-top: 5px;
                    border-radius: 3px;
                    border: 1px solid #bdc3c7;
                }
                .footer {
                    font-size: 12px;
                    color: #7f8c8d;
                    margin-top: 20px;
                    padding-top: 20px;
                    border-top: 1px solid #ecf0f1;
                    text-align: center;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Solicitud Rechazada</h1>
                </div>
                <div class="content">
                    <p>Hola,</p>

                    <p>Lamentablemente, la solicitud de tu organización ha sido <strong>rechazada</strong>.</p>

                    <div class="motivo-box">
                        <div class="label">Organización:</div>
                        <div class="value">""" + nombreOrganizacion + """
</div>

                        <div class="label">Motivo del rechazo:</div>
                        <div class="value">""" + motivo + """
</div>
                    </div>

                    <p>Si tenés dudas sobre esta decisión o querés presentar una apelación, contactá al equipo de VIVSO.</p>

                    <div class="footer">
                        <p>&copy; 2026 VIVSO - Todos los derechos reservados</p>
                    </div>
                </div>
            </div>
        </body>
        </html>
        """;
    }

}