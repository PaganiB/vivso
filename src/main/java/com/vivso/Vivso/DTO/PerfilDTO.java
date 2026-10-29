package com.vivso.Vivso.DTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PerfilDTO {
    private Integer id;
    private String username;
    private String email;
    private String rol;
    private String nombre;       // solo integrantes
    private String apellido;     // solo integrantes
    private String cargo;        // PRESIDENTE / TESORERO, solo integrantes
    private OrganizacionResumenDTO organizacion;   // solo integrantes
}
