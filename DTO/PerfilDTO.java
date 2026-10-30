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
    private String nombre;
    private String apellido;
    private String cargo;
    private OrganizacionResumenDTO organizacion;
}
