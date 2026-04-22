package com.vivso.Vivso.DTO;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
//DTO para retornar como respuesta
public class UsuarioRespuestaDTO {
    private Integer id;
    private String username;
    private String email;
    private String rol;
}