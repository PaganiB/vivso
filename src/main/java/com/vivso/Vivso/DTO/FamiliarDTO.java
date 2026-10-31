package com.vivso.Vivso.DTO;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FamiliarDTO {

    private Integer id_familiar;
    private String dni;
    private String nombre;
    private String apellido;
    private String condicion_especial;
    private Integer familia;
}