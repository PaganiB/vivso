
package com.vivso.Vivso.Exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Excepciones propias de VIVSO
    @ExceptionHandler(VivsoException.class)
    public ResponseEntity<ErrorResponse> manejarVivsoException(VivsoException ex) {

        ErrorResponse error = new ErrorResponse(
                ex.getStatus().value(),
                ex.getMessage()
        );

        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    // Validaciones de DTO mediante @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidaciones(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(errores);
    }

    // Errores de binding
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, String>> manejarBindException(
            BindException ex) {

        Map<String, String> errores = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(errores);
    }

    // Validaciones de parámetros de métodos
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> manejarValidacionParametros(
            HandlerMethodValidationException ex) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Uno o más parámetros de la solicitud son inválidos"
        );

        return ResponseEntity.badRequest().body(error);
    }

    // Errores inesperados
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> manejarErrorGlobal(Exception ex) {

        log.error("Error inesperado en VIVSO", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ocurrió un error inesperado en el sistema VIVSO. Por favor, contacte al administrador.");
    }
}
