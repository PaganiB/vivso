package com.vivso.Vivso.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@Builder
public class VisitaHistorialDTO {
    private String id;               // Ej: "inicial_1" o "obra_5" para diferenciarlas
    private String tipo;             // "INICIAL" o "OBRA"
    private String expediente;       // Número de expediente (o identificador de la vivienda)
    private String localidad;
    private String departamento;
    private String fecha;            // Formateada como "DD/MM/YYYY"
    private String clasificacion;    // Ej: "2A" (para inicial) o "-" (para obra)
    private String estado;           // "sincronizada" (vienen de la BD)
    private Integer avanceActual;    // 0% para inicial, o el % de AFO para obra

    // --- CAMPOS DETALLADOS PARA LA VISTA DE DETALLE ---
    private Integer cantHabitantesReal;
    private Boolean tienePrioridadSocial;
    private String observaciones;
    private Double lat;
    private Double lng;
    private List<String> urlsFotos;
    private String tecnicoEmail;
}