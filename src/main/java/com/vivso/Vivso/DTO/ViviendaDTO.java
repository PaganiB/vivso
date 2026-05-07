package com.vivso.Vivso.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vivso.Vivso.Model.ClasificacionVivienda;
import com.vivso.Vivso.Model.EstadoVivienda;
import com.vivso.Vivso.Model.TipoVivienda;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViviendaDTO {

    @NotBlank(message = "El número de expediente es obligatorio")
    @Size(max = 50, message = "El número de expediente no puede superar los 50 caracteres")
    private String numExp;

    @NotBlank(message = "El departamento es obligatorio")
    private String departamento;

    @NotBlank(message = "La localidad es obligatoria")
    private String localidad;

    private String barrio;

    private String direccion;

    @NotNull(message = "La superficie es obligatoria")
    @DecimalMin(value = "0.1", message = "La superficie debe ser un valor positivo")
    private BigDecimal superficie;

    @NotNull(message = "Debe ingresar la fecha de inicio ")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate fechaInic;

    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate fechaFin; // Opcional si sigue en obra

    @NotNull(message = "El estado de la vivienda es obligatorio")
    private EstadoVivienda estado;

    private String observacion;

    @NotNull(message = "Debe ingresar la familia asociada")
    private Integer id_familia;

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe ser mayor o igual a -90")
    @DecimalMax(value = "90.0", message = "La latitud debe ser menor o igual a 90")
    private Double lat;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe ser mayor o igual a -180")
    @DecimalMax(value = "180.0", message = "La longitud debe ser menor o igual a 180")
    private Double lng;

    private String representante;

    @Min(0) @Max(100)
    private Integer avanceObra; // El AFO

    @NotNull(message = "La clasificación es obligatoria")
    private ClasificacionVivienda clasificacion;

    @NotNull(message = "El tipo de vivienda es obligatorio")
    private TipoVivienda tipoVivienda;

    @Min(0)
    private Integer cantDormitorios;
}
