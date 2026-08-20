package com.vivso.Vivso.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TipoDocumento {
    // organizacion
    ACTA_COMPROMISO_ONG("Acta Compromiso de la organizacion"),
    NOTA_SOLICITUD("Nota de solicitud"),
    ALTA_AFIP("Alta de AFIP"),
    ACTA_ASAMBLEA("Acta de Asamblea Actual"),
    CONSTANCIA_VIGENCIA("Constancia de vigencia"),
    CONSTANCIA_CUENTA_BANCARIA("Constancia de cuenta bancaria (BSE)"),

    DNI_PRESIDENTE("DNI PRESIDENTE"),
    DNI_TESORERO("DNI TESORERO"),
    CERTIFICADO_RESIDENCIA_PRESIDENTE("Certificado de Residencia del Presidente"),
    CERTIFICADO_RESIDENCIA_TESORERO("Certificado de Residencia de tesorero"),
    // familia
    DNI_FAMILIARES("DNI FAMILIAR"),
    CERTIFICADO_DISCAPACIDAD("Certificado de Discapacidad"),
    ESCRITURA_PROPIEDAD("Escritura de la propiedad"),
    ACTA_COMPROMISO_FAMILIA("Acta Compromiso"),
    FOTOS_TERRENO("Foto del Terreno"),
    CERTIFICADO_RESIDENCIA_FAMILIA("Certificado de Residencia  de familia"),
    DECLARACION_JURADA("Declaración Jurada"),

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
