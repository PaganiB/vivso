package com.vivso.Vivso.DTO;

import com.vivso.Vivso.Model.TipoVivienda;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViviendaRegistroDTO {

    @NotBlank(message = "El departamento es obligatorio")
    private String departamento;

    @NotBlank(message = "La localidad es obligatoria")
    private String localidad;

    private String barrio;
    private String direccion;

    @NotNull(message = "La superficie es obligatoria")
    @DecimalMin(value = "0.1")
    private BigDecimal superficie;

    private LocalDate fechaInic;
    private LocalDate fechaFin;

    @NotNull(message = "Debe ingresar la familia asociada")
    private Integer idFamilia;

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double lat;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double lng;

    @NotNull(message = "El tipo de vivienda es obligatorio")
    private TipoVivienda tipoVivienda;

    @Min(0)
    private Integer cantDormitorios;

    private Integer solicitud;
}