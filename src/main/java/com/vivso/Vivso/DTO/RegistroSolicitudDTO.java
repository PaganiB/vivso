package com.vivso.Vivso.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroSolicitudDTO {

    @NotNull(message = "Los datos de la familia son obligatorios")
    @Valid
    private FamiliaRegistroDTO familia;

    @NotEmpty(message = "Debe registrar al menos un integrante de la familia")
    @Valid
    private List<FamiliarRegistroDTO> familiares;
}