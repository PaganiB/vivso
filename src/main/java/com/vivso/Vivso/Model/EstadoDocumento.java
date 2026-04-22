package com.vivso.Vivso.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum EstadoDocumento {
    PENDIENTE("Pendiente"),
    EN_REVISION("En revisión"),
    APROBADO("Aprobado"),
    RECHAZADO("Rechazado");

    private final String valor;

    EstadoDocumento(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static EstadoDocumento fromString(String value) {
        for (EstadoDocumento estado : EstadoDocumento.values()) {
            if (estado.valor.equalsIgnoreCase(value) || estado.name().equalsIgnoreCase(value)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de documento no válido: " + value);
    }
}
