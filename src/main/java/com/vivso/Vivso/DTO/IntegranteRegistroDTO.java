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
public class IntegranteRegistroDTO {

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(
            regexp = "^\\d{7,8}$",
            message = "El DNI debe tener entre 7 y 8 números"
    )
    private String dni;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50)
    private String apellido;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(min = 10, max = 20)
    private String telefono;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo electrónico no tiene un formato válido")
    @Size(max = 250)
    private String correo;

    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @NotNull(message = "La fecha de alta del cargo es obligatoria")
    private LocalDate fechaAltaCargo;

    @NotBlank(message = "El domicilio es obligatorio")
    @Size(min = 3, max = 150)
    private String domicilio;
}