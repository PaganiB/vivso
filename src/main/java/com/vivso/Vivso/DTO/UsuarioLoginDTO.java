package com.vivso.Vivso.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioLoginDTO {

    /*@NotBlank(message = "El username es obligatorio")
    private String username;*/

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email debe ser válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}