package com.vivso.Vivso.DTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrganizacionResumenDTO {
    private String cuit;
    private String nombre;
    private String presidente; //si
    private String tesorero;
}