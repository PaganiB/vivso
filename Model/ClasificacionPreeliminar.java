package com.vivso.Vivso.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ClasificacionPreeliminar {
    TIPO_1A("1a"), //Estandar
    TIPO_2A("2a"), //Economica
    TIPO_2B("2b"), // Aparece en el reporte del VISOC
    TIPO_3A("3a"),
    TIPO_4A("4a"),
    TIPO_4B("4b"),
    TIPO_4C("4c"),
    TIPO_5A("5a"),
    TIPO_5B("5b"),
    TIPO_5C("5c"),
    TIPO_5D("5d"),
    DERRUMBE("DERRUMBE"),
    OTRA("OTRA");

    private final String valor;

    ClasificacionPreeliminar(String valor) {
        this.valor = valor;
    }

    @JsonValue // Para que el JSON devuelva "1a" en lugar de "TIPO_1A"
    public String getValor() {
        return valor;
    }

    @JsonCreator // Para que acepte "1a", "1A" o "tipo_1a" desde el frontend
    public static ClasificacionPreeliminar fromString(String value) {
        if (value == null) return null;

        for (ClasificacionPreeliminar clasif : ClasificacionPreeliminar.values()) {
            if (clasif.valor.equalsIgnoreCase(value) || clasif.name().equalsIgnoreCase(value)) {
                return clasif;
            }
        }
        throw new IllegalArgumentException("Clasificación de vivienda no válida: " + value);
    }
}