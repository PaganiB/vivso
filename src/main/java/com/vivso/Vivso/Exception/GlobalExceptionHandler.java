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
    public ResponseEntity<String> manejarRuntime(RuntimeException ex) {
        String mensaje = ex.getMessage().toLowerCase();

        // Si en el Service pusiste "no encontrado"
        if (mensaje.contains("no encontrado")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }

        // Si en el Service pusiste "ya existe" o "ya está registrado"
        if (mensaje.contains("ya existe") || mensaje.contains("registrado")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }

        // Para cualquier otro error de lógica de negocio
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
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
