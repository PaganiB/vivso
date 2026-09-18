package com.vivso.Vivso.Exception;

import org.springframework.http.HttpStatus;

public abstract class VivsoException extends RuntimeException {

    private final HttpStatus status;

    protected VivsoException(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}