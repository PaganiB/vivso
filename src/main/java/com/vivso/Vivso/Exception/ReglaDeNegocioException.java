package com.vivso.Vivso.Exception;

import org.springframework.http.HttpStatus;

public class ReglaDeNegocioException extends VivsoException {

    public ReglaDeNegocioException(String mensaje, HttpStatus status) {
        super(mensaje, status);
    }
}