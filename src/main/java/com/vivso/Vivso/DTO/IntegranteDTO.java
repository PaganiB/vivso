package com.vivso.Vivso.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vivso.Vivso.Model.TipoCargo;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

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

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Debe ingresar un formato de correo válido (ejemplo@dominio.com)")
    private String correo;

    @NotNull(message = "El cargo en la institución es obligatorio")
    private TipoCargo cargo;

    @NotNull(message = "La fecha de alta del cargo es obligatoria")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate fechaAltaCargo;

    @NotBlank(message = "El domicilio es obligatorio")
    private String domicilio;

    @NotNull(message = "Se debe indicar si el integrante se encuentra activo o no")
    private Boolean activo;

    @Size(max = 50, message = "El nombre de usuario no puede superar los 50 caracteres")
    private String usuario;

    @NotBlank(message = "Es cuit de la organizacion a la que pertenece es obligatorio")
    private String cuitOrg;

    private String nombreOrganizacion; // Solo para lectura en el front


}
