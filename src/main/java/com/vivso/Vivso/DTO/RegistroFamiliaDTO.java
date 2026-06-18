package com.vivso.Vivso.DTO;

import jakarta.validation.Valid;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroFamiliaDTO {

    // === TEXTO ===
    @Valid private FamiliaDTO familia;
    @Valid private List<FamiliarDTO> familiares; // la lista de integrantes del grupo familiar
    @Valid private SolicitudDTO solicitud;       // necesitás el cuitOrg para saber qué org presenta

    // === ARCHIVOS (docs de la familia según el flyer) ===
    // ACTA_COMPROMISO, DNI (familiares), CERTIFICADO_RESIDENCIA,
    // ESCRITURA_PROPIEDAD o DECLARACION_JURADA_POSEEDOR, CERTIFICADO_DISCAPACIDAD, FOTO_TERRENO

}
