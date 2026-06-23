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
    @Valid private IntegranteDTO tesorero; // opcional, como ya tenías

    // === ARCHIVOS (docs de la org según el flyer) ===
    // Los archivos viajan por el MultiValueMap, no hace falta declararlos acá
    // NOTA_SOLICITUD, ACTA_DE_ASAMBLEA_ACTUAL_HCD, CONSTANCIA_CUENTA_BANCARIA,
    // ALTA_AFIP, DNI (presidente y tesorero), CERTIFICADO_RESIDENCIA
}