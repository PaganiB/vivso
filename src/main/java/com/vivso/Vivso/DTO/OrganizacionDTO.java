package com.vivso.Vivso.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrganizacionDTO {

    @NotBlank(message = "El CUIT es obligatorio")
    @Pattern(regexp = "^(30|33)-\\d{8}-\\d{1}$", message = "El CUIT debe tener el formato XX-XXXXXXXX-X, solo se admiten cuit de empresas 30 o 33")
    private String cuit;

    @NotBlank(message = "El nombre de la organización es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "Especifique el tipo de organización (Ej: ONG, Asociación, Cooperativa)")
    private String tipo;

    @NotBlank(message = "El domicilio legal es obligatorio para notificaciones")
    private String dom_legal;

    @NotBlank(message = "Debe proporcionar un teléfono o email de contacto")
    private String contacto;

    @NotBlank(message = "El Código Postal (CPE) es obligatorio")
    private String cpe;
}
