package com.vivso.Vivso.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @NotBlank(message = "El Codigo del GDE es obligatorio")
    private String GDE;

    @NotNull(message = "Fecha de solicitud obligatoria")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate fechaSolicitud;

    @NotBlank(message = "El cuit es obligatorio")
    private String cuitOrg;

    private String numExp;

    @NotNull(message = "Debe indicar el estado de la solicitud (Pendiente, Aprobada o Rechazada)")
    private EstadoSolicitud estado;

    @Size(max = 200, message = "Los caracteres máximos son 200")
    private String observacion;

}