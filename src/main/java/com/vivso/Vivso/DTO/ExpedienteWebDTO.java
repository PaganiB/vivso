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

    // Este valor ahora vendrá estrictamente del celular (0 a 100)
    private Integer progreso;
    private String ultimaVisita;

    // El frontend lee este estado para saber si dibuja la barra (Aprobada) o no (Pendiente)
    private EstadoSolicitud estadoSolicitud;
}