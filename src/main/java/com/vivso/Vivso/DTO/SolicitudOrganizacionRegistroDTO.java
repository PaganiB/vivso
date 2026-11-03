package com.vivso.Vivso.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudOrganizacionRegistroDTO {

    @NotNull(message = "Los datos de la organización son obligatorios")
    @Valid
    private OrganizacionRegistroDTO organizacion;

    @NotNull(message = "Los datos del presidente son obligatorios")
    @Valid
    private IntegranteRegistroDTO presidente;

    @NotNull(message = "Los datos del tesorero son obligatorios")
    @Valid
    private IntegranteRegistroDTO tesorero;
}