package com.vivso.Vivso.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioRespuestaDTO {
    private Integer id;
    private String username;
    private String email;
    private String rol;
}