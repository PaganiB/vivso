package com.vivso.Vivso.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizacionRegistroDTO {

    @NotBlank(message = "El CUIT es obligatorio")
    //@Pattern(regexp = "^(30|33)-\\d{8}-\\d{1}$", message = "El CUIT debe tener el formato XX-XXXXXXXX-X")
    private String cuit;

    @NotBlank(message = "El nombre de la organización es obligatorio")
    @Size(min = 2, max = 100)
    private String nombre;

    @NotBlank(message = "El tipo de organización es obligatorio")
    @Size(min = 2, max = 100)
    private String tipo;

    @NotBlank(message = "El domicilio legal es obligatorio")
    @Size(min = 3, max = 150)
    private String dom_legal;

    @NotBlank(message = "El número de CPE es obligatorio")
    private String cpe;

    // Comentada hasta que javi cambie el formato de fecha en el front, sino peta
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @NotNull(message = "La fecha de vencimiento de vigencia es obligatoria")
    private LocalDate fechaVencimientoVigencia;

    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    //@NotNull(message = "La fecha de última asamblea es obligatoria")
    private LocalDate fechaUltimaAsamblea;
}