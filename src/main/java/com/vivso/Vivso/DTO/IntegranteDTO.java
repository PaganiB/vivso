package com.vivso.Vivso.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IntegranteDTO {

    private Integer idIntegrante;

    @NotBlank (message = "El dni es obligatorio")
    @Pattern(regexp = "^\\d{7,8}$", message = "El DNI debe tener entre 7 y 8 números, sin puntos ni espacios")
    private String dni;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String apellido;

    @NotBlank(message = "Debe proporcionar un telefono de contacto")
    private String telefono;

    @NotBlank(message = "La funcion es obligatoria")
    private String funcion;

    @NotBlank(message = "El domicilio es obligatorio")
    private String domicilio;

    @NotNull(message = "Se debe indicar si el integrante se encuentra activo o no")
    private Boolean activo;

    @Size(max = 50, message = "El nombre de usuario no puede superar los 50 caracteres")
    private String usuario;

    @NotBlank(message = "Es cuit de la organizacion a la que pertenece es obligatorio")
    private String cuitOrg;

}
