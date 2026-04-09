package com.vivso.Vivso.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum EstadoVivienda {
    ADJUDICADA("Adjudicada"),
    INICIADA("Iniciada"),
    AVANZADA("Avanzada"),
    FINALIZADA("Finalizada");

    private final String valor;

    EstadoVivienda(String valor) {
        this.valor = valor;
    }

    @JsonValue // Esto hace que en el JSON de respuesta se vea "Adjudicada" en lugar de "ADJUDICADA"
    public String getValor() {
        return valor;
    }

    @JsonCreator // Esto permite que si mandan "adjudicada" (minúscula) lo entienda igual
    public static EstadoVivienda fromString(String value) {
        for (EstadoVivienda estado : EstadoVivienda.values()) {
            if (estado.valor.equalsIgnoreCase(value) || estado.name().equalsIgnoreCase(value)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de vivienda no válido: " + value);
    }
}
