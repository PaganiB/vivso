package com.vivso.Vivso.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. MANEJO DE EXCEPCIONES DE LÓGICA (RuntimeException)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> manejarRuntime(RuntimeException ex) {
        String mensaje = ex.getMessage().toLowerCase();
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("error", ex.getMessage()); // Mantenemos el mensaje original aquí

        HttpStatus status = HttpStatus.BAD_REQUEST; // Por defecto 400

        if (mensaje.contains("no encontrado")) {
            status = HttpStatus.NOT_FOUND; // 404
        } else if (mensaje.contains("ya existe") || mensaje.contains("registrado")) {
            status = HttpStatus.CONFLICT; // 409
        }

        return ResponseEntity.status(status).body(respuesta);
    }

    // 2. MANEJO DE VALIDACIONES DE DTO (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();

        // Extraemos campo por campo qué falló (ej: email inválido)
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errores.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }

    // 3. ERROR GENÉRICO (El "Safe Net")
    /*@ExceptionHandler(Exception.class)
    public ResponseEntity<String> manejarErrorGlobal(Exception ex) {
        // Log para que tú veas qué pasó en la consola
        System.out.println("LOG VIVSO - Error Inesperado: " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ocurrió un error inesperado en el sistema VIVSO. Por favor, contacte al administrador.");
    }*/
}
