package com.vivso.Vivso.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TipoDocumento {
    DNI_FAMILIAR("DNI FAMILIAR"),
    DNI_TESORERO("DNI TESORERO"),
    DNI_PRESIDENTE("DNI PRESIDENTE"),
    CERTIFICADO_DISCAPACIDAD("Certificado de Discapacidad"),
    CERTIFICADO_RESIDENCIA("Certificado de Residencia"),
    ESCRITURA_PROPIEDAD("Escritura de la propiedad"),
    DECLARACION_JURADA_POSEEDOR("Declaración Jurada"),
    ACTA_COMPROMISO("Acta Compromiso"),
    FOTO_TERRENO("Foto del Terreno"),
    CONSTANCIA_CUENTA_BANCARIA("Constancia de cuenta bancaria (BSE)"),
    ALTA_AFIP("Alta de AFIP"),
    NOTA_SOLICITUD("Nota de solicitud"),
    ACTA_ASAMBLEA("Acta de Asamblea Actual"),
    CONSTANCIA_VIGENCIA("Constancia de vigencia"),
    OTRO("Otro");

    private final String valor;

    TipoDocumento(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static TipoDocumento fromString(String value) {
        for (TipoDocumento tipo : TipoDocumento.values()) {
            if (tipo.valor.equalsIgnoreCase(value) || tipo.name().equalsIgnoreCase(value)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de documento no válido: " + value);
    }
}
