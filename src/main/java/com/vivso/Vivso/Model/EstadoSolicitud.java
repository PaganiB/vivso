package com.vivso.Vivso.Model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EstadoSolicitud {
    Pendiente,
    Aprobada,
    Rechazada;

    @JsonCreator
    public static EstadoSolicitud from(String value) {
        try {
            return EstadoSolicitud.valueOf(
                    value.substring(0,1).toUpperCase() + value.substring(1).toLowerCase()
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Estado inválido: " + value);
        }
    }
}