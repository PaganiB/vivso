package com.vivso.Vivso.DTO;

import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizacionDesarrolloSocialDTO {

    // Datos de la organización
    private String cuit;
    private String nombre;
    private String tipo;
    private String dom_legal;
    private String cpe;

    // Integrantes de la organización
    private List<IntegranteDTO> integrantes;

    // Solicitudes y proyectos asociados
    private List<ProyectoOrganizacionDTO> proyectos;
}