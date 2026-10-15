package com.vivso.Vivso.Exception;

import java.time.LocalDateTime;

public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String mensaje;

    public ErrorResponse(int status, String mensaje) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.mensaje = mensaje;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getMensaje() { return mensaje; }
}