package com.vivso.Vivso.Service;

public interface IEmailService {

    // Envía un email con las credenciales y notifica que la solicitud fue aprobada
    void enviarCredencialesYAprobacion(String email, String username, String passwordTemporal, String nombreOrganizacion);

    // Notifica que una solicitud de organización fue rechazada
    void enviarNotificacionSolicitudRechazada(String email, String nombreOrganizacion, String motivo);
}
