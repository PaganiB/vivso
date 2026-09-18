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

    //@NotNull(message = "Fecha de solicitud obligatoria")
    private LocalDate fechaSolicitud;

    //@NotBlank(message = "El CUIT de la organización es obligatorio")
    private String cuitOrg;

    private String numExp;

    //@NotNull(message = "Debe indicar el estado de la solicitud (Pendiente, Aprobada o Rechazada)")
    private EstadoSolicitud estado;

    @Size(max = 200, message = "Los caracteres máximos son 200")
    private String observacion;

    //@NotNull(message = "Debe indicar el ID de la familia solicitante")
    private Integer idFamilia;

    private Integer idVivienda;

    private String nombreRepresentanteFamilia;

    private String nombreOrganizacion;
}