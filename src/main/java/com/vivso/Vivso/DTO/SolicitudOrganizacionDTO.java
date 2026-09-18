package com.vivso.Vivso.DTO;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudOrganizacionDTO {

    private Integer idSolicitudOrg;

    private String cuit;
    private String nombre;
    private String tipo;
    private String domLegal;
    private String cpe;
    private LocalDate fechaVencimientoVigencia;
    private LocalDate fechaUltimaAsamblea;

    private String dniPresidente;
    private String nombrePresidente;
    private String apellidoPresidente;
    private String correoPresidente;
    private String telefonoPresidente;
    private LocalDate fechaAltaCargoPresidente;
    private String domicilioPresidente;

    private String dniTesorero;
    private String nombreTesorero;
    private String apellidoTesorero;
    private String correoTesorero;
    private String telefonoTesorero;
    private LocalDate fechaAltaCargoTesorero;
    private String domicilioTesorero;

    private List<DocumentoDTO> documentos;

    private String estado;
    private String motivo;
    private String camposObservados;
    private LocalDate fechaRechazo;
    private LocalDate fechaAprobacion;
    private LocalDate fechaSolicitud;
}