package com.vivso.Vivso.DTO;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpedienteMovilDTO {
    private Integer idSolicitud;
    private String numExp;
    private String nombreRepresentanteFamilia;
    private String nombreOrganizacion;
}