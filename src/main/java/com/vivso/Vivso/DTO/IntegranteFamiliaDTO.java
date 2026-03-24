package com.vivso.Vivso.DTO;

import com.vivso.Vivso.Model.Familia;
import com.vivso.Vivso.Model.Usuario;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IntegranteFamiliaDTO {
    private Integer id;
    private String dni;
    private String nombre;
    private String apellido;
    private String condicion_especial;
    private String familia;
    private String usuario;
}
