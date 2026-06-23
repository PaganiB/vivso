package com.vivso.Vivso.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TipoCargo {
    PRESIDENTE("Presidente"),
    VICEPRESIDENTE("Vicepresidente"),
    TESORERO("Tesorero"),
    SECRETARIO("Secretario"),
    VOCAL("Vocal"),
    INTEGRANTE("Integrante");

    private final String valor;

    TipoCargo(String valor) {
        this.valor = valor;
    }

    @JsonValue // En el JSON de respuesta se verá "Presidente"
    public String getValor() {
        return valor;
    }

    @JsonCreator // Entiende "presidente", "PRESIDENTE" o "Presidente"
    public static TipoCargo fromString(String value) {
        if (value == null) return null;

        for (TipoCargo cargo : TipoCargo.values()) {
            if (cargo.valor.equalsIgnoreCase(value) || cargo.name().equalsIgnoreCase(value)) {
                return cargo;
            }
        }
        throw new IllegalArgumentException("Cargo de organización no válido: " + value);
    }
}
