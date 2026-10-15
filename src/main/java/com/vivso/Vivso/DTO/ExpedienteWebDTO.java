package com.vivso.Vivso.DTO;

import com.vivso.Vivso.Model.EstadoSolicitud;
import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpedienteWebDTO {
    private Integer idSolicitud;
    private String numExp;
    private String estadoRelevamiento;
    private String beneficiario;
    private String dni;

    private String etapaActual;
    private String ultimoRegistro;

    // El frontend lee este estado para saber si dibuja la barra (Aprobada) o no (Pendiente)
    private EstadoSolicitud estadoSolicitud;
}