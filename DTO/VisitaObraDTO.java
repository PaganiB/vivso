package com.vivso.Vivso.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitaObraDTO {

    private Integer idVisitaObra;

    @NotNull(message = "La fecha de la visita es obligatoria")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate fecha;

    @NotNull(message = "El AFO (Avance Físico de Obra) es obligatorio")
    @Min(value = 0, message = "El AFO no puede ser menor a 0%")
    @Max(value = 100, message = "El AFO no puede ser mayor a 100%")
    private Integer afo;

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe ser válida")
    @DecimalMax(value = "90.0", message = "La latitud debe ser válida")
    private Double lat;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe ser válida")
    @DecimalMax(value = "180.0", message = "La longitud debe ser válida")
    private Double lng;

    @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres")
    private String observaciones;

    // --- Identificadores para las relaciones ---

    @NotNull(message = "El ID de la vivienda es obligatorio")
    private Integer idVivienda;

    @NotNull(message = "El ID del técnico es obligatorio")
    private Integer idTecnico;
}