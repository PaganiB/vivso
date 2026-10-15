package com.vivso.Vivso.DTO;

import jakarta.validation.Valid;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroOrganizacionDTO {

    // === TEXTO ===
    @Valid private OrganizacionDTO organizacion;
    @Valid private IntegranteDTO presidente;
    @Valid private IntegranteDTO tesorero;

}