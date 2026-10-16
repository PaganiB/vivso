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
}
