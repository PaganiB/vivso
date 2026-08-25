package com.vivso.Vivso.Exception;

import org.springframework.http.HttpStatus;

public abstract class VivsoException extends RuntimeException {

    public VivsoException(String mensaje) {
        super(mensaje);
    }

    public abstract HttpStatus getStatus();
}