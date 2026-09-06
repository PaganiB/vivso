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

    @ExceptionHandler(VivsoException.class)
    public ResponseEntity<ErrorResponse> manejarVivsoException(VivsoException ex) {
        ErrorResponse error = new ErrorResponse(ex.getStatus().value(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> manejarRuntime(RuntimeException ex) {

        ex.printStackTrace(); // <-- agregar esto

        String mensaje = ex.getMessage().toLowerCase();

        if (mensaje.contains("no encontrado")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }

        if (mensaje.contains("ya existe") || mensaje.contains("registrado")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }

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
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> manejarErrorGlobal(Exception ex) {
        // Log para que tú veas qué pasó en la consola
        System.out.println("LOG VIVSO - Error Inesperado: " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ocurrió un error inesperado en el sistema VIVSO. Por favor, contacte al administrador.");
    }
}
