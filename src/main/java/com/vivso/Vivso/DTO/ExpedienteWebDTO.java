package com.vivso.Vivso.DTO;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpedienteWebDTO {
    private Integer idSolicitud;
    private String numExp;
    private String beneficiario;
    private String dni;

    // Datos dinámicos para las tarjetas de la web
    private String estadoRelevamiento; // "Sin relevamiento" o "Relevamiento en proceso"
    private String ultimaVisita;       // Fecha o "Sin relevamiento"
    private Integer progreso;          // 0 a 100
}