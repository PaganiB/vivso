package com.vivso.Vivso.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FamiliarDTO {

    private Integer idIntegranteFamilia;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^\\d{7,8}$", message = "El DNI debe tener entre 7 y 8 números, sin puntos ni espacios")
    private String dni;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String apellido;

    // Este lo dejamos opcional, pero con un límite de tamaño
    @Size(max = 255, message = "La descripción es demasiado larga")
    private String condicion_especial;

    @NotNull(message = "El ID de la familia es obligatorio para el vínculo")
    private Integer familia;

    @Size(max = 50, message = "El nombre de usuario no puede superar los 50 caracteres")
    private String usuario;
}
