package com.vivso.Vivso.DTO;

import com.vivso.Vivso.Model.EstadoSolicitud;
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
    private EstadoSolicitud estado;
}