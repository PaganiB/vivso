package com.vivso.Vivso.DTO;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Entrada: la ONG envía la etapa a revisión. Las fotos se suben aparte (FotoEtapa).
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnviarEtapaDTO {

    @Size(max = 500, message = "La observación no puede superar los 500 caracteres")
    private String observacionOrganizacion;
}