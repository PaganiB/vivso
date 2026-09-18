package com.vivso.Vivso.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroFamiliaDTO {

    @Valid private FamiliaDTO familia;
    @Valid private List<FamiliarDTO> familiares;

    // De la solicitud solo necesitás el cuitOrg. La observacion,
    //  La observacion, el idFamilia, estado y fecha los pone el back
    @NotBlank(message = "El cuit de la organizacion es obligatorio")
    private String cuitOrg;

}
