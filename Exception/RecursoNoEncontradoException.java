package com.vivso.Vivso.Exception;

import org.springframework.http.HttpStatus;

public class RecursoNoEncontradoException extends VivsoException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje, HttpStatus.NOT_FOUND);
    }
}