package com.vivso.Vivso.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vivso.Vivso.Model.EstadoVivienda;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    private String representante;
}
