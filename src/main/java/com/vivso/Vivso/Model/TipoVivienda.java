package com.vivso.Vivso.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TipoVivienda {
    URBANA("Urbana"),
    RURAL("Rural"),
    ECONOMICA("Económica");

    private final String valor;

    TipoVivienda(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static TipoVivienda fromString(String value) {
        if (value == null) return null;

        for (TipoVivienda tipo : TipoVivienda.values()) {
            // Esto permite que si en la base de datos vieja dice "URB",
            // el sistema nuevo lo entienda como "URBANA"
            if (tipo.valor.equalsIgnoreCase(value) ||
                    tipo.name().equalsIgnoreCase(value) ||
                    (value.equalsIgnoreCase("URB") && tipo == URBANA) ||
                    (value.equalsIgnoreCase("RUR") && tipo == RURAL)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de vivienda no válido: " + value);
    }
}
