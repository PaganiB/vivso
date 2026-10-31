package com.vivso.Vivso.DTO;

import com.vivso.Vivso.Model.EstadoSolicitud;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudDTO {

    // Información general
    private Integer idSolicitud;
    private LocalDate fechaSolicitud;
    private String numExp;
    private EstadoSolicitud estado;

    // Organización
    private String cuitOrg;
    private String nombreOrganizacion;

    // Revisión administrativa
    private String observacion;
    private String camposObservados;

    // Información de la familia
    private Integer idFamilia;
    private FamiliaDTO familia;
    private List<FamiliarDTO> familiares;

    // Documentación presentada
    private List<DocumentoDTO> documentos;

    // Asignación
    private Integer idTecnico;
}