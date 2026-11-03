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
public class ProyectoOrganizacionDTO {

    // Datos de la solicitud
    private Integer idSolicitud;
    private String numeroExpediente;
    private LocalDate fechaSolicitud;
    private EstadoSolicitud estadoSolicitud;

    // Familia beneficiaria
    private FamiliaDTO familia;
    private List<FamiliarDTO> familiares;

    // Vivienda
    private ViviendaDTO vivienda;

    // Seguimiento de obra
    private List<EtapaObraDTO> etapasObra;
    private String estadoGeneralObra;
}