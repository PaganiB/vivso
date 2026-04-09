package com.vivso.Vivso.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//DTO para retornar como respuesta
public class UsuarioRespuestaDTO {
    private Integer id;
    private String username;
    private String email;
    private String rol;
}