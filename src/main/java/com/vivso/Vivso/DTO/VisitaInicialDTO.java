package com.vivso.Vivso.DTO;

import com.vivso.Vivso.Model.ClasificacionVivienda;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitaInicialDTO {

    private Integer idVisita;

    @NotNull(message = "La Fecha de la visita es obligatoria")
    @PastOrPresent(message = "No se puede tener una visita de fecha futura")
    private LocalDateTime fechaVisita;

    @NotNull(message = "La solicitud es obligatoria")
    private Integer idSolicitud;

    @NotNull(message = "El tecnico es obligatorio")
    private Integer idTecnico;

    @NotNull(message = "Se debe otorgar una clasificacion a la vivienda")
    private ClasificacionVivienda clasificacionPreliminar;

    @NotNull(message = "la Latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud mínima es -90")
    @DecimalMax(value = "90.0", message = "La latitud máxima es 90")
    private Double lat;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud mínima es -180")
    @DecimalMax(value = "180.0", message = "La longitud máxima es 180")
    private Double lng;

    @Size(max = 150)
    private String observaciones;

    private String localidad;
    private String departamento;

    @NotNull(message = "Se debe indicar la cantidad de habitantes")
    @Positive(message = "La cantidad de habitantes no puede ser negativa")
    private Integer cantHabitantesReal;

    @NotNull(message = "Debe indicar si tiene prioridad social o no")
    private Boolean tienePrioridadSocial;

    private List<String> urlsFotos;
}