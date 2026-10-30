package com.vivso.Vivso.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Entrada: el técnico devuelve la etapa a la ONG. El motivo es obligatorio.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ObservarEtapaDTO {

    @NotBlank(message = "Debe indicar el motivo de la observación")
    @Size(max = 500, message = "La observación no puede superar los 500 caracteres")
    private String observacionTecnico;
}