package com.vivso.Vivso.DTO;

import com.vivso.Vivso.Model.EstadoSolicitud;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SolicitudDTO {

    private Integer idSolicitud;

    private LocalDate fechaSolicitud;

    private String cuitOrg;

    private String numExp;

    private EstadoSolicitud estado;

    @Size(max = 200, message = "Los caracteres máximos son 200")
    private String observacion;

    private String camposObservados;

    private Integer idFamilia;

    private String nombreRepresentanteFamilia;

    private String nombreOrganizacion;

    private Integer idTecnico;
}