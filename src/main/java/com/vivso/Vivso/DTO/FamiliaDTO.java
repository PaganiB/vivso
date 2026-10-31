package com.vivso.Vivso.DTO;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FamiliaDTO {

    private Integer idFamilia;
    private String nombreRepresentante;
    private String dniRepresentante;
    private String contacto;
    private Integer antiguedadRancho;
    private String referenciaUbicacion;
    private Double latitud;
    private Double longitud;
}