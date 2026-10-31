package com.vivso.Vivso.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamiliaRegistroDTO {

    @NotBlank(message = "El nombre del representante es obligatorio")
    @Size(max = 50)
    private String nombreRepresentante;

    @NotBlank(message = "El DNI del representante es obligatorio")
    @Pattern(regexp = "^\\d{7,8}$")
    private String dniRepresentante;

    @NotBlank(message = "El contacto es obligatorio")
    @Size(max = 100)
    private String contacto;

    @NotNull(message = "La antigüedad del rancho es obligatoria")
    @Min(value = 5)
    private Integer antiguedadRancho;

    @Size(max = 255)
    private String referenciaUbicacion;

    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitud;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitud;
}