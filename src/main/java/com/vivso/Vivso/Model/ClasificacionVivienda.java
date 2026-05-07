package com.vivso.Vivso.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ClasificacionVivienda {
    TIPO_1A("1a"),
    TIPO_2A("2a"),
    TIPO_2B("2b"), // Aparece en el reporte del VISOC
    TIPO_5F("5f"), // Aparece en el reporte del VISOC
    DERRUMBE("DERRUMBE"),
    OTRA("OTRA");

    private final String valor;

    ClasificacionVivienda(String valor) {
        this.valor = valor;
    }

    @JsonValue // Para que el JSON devuelva "1a" en lugar de "TIPO_1A"
    public String getValor() {
        return valor;
    }

    @JsonCreator // Para que acepte "1a", "1A" o "tipo_1a" desde el frontend
    public static ClasificacionVivienda fromString(String value) {
        if (value == null) return null;

        for (ClasificacionVivienda clasif : ClasificacionVivienda.values()) {
            if (clasif.valor.equalsIgnoreCase(value) || clasif.name().equalsIgnoreCase(value)) {
                return clasif;
            }
        }
        throw new IllegalArgumentException("Clasificación de vivienda no válida: " + value);
    }
}