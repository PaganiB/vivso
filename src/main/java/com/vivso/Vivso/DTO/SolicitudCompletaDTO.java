package com.vivso.Vivso.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudCompletaDTO {

    @Valid
    @NotNull(message = "Los datos de la organización son obligatorios")
    private OrganizacionDTO organizacion;

    @Valid
    @NotNull(message = "Los datos del presidente son obligatorios")
    private IntegranteDTO presidente;

    @Valid
    @NotNull(message = "Los datos del tesorero son obligatorios")
    private IntegranteDTO tesorero;

    @Valid
    @NotNull(message = "Los datos de la familia son obligatorios")
    private FamiliaDTO familia;

    @Valid
    @NotNull(message = "Los datos de la solicitud son obligatorios")
    private SolicitudDTO solicitud;
}