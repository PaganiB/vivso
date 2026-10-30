package com.vivso.Vivso.DTO;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ProrrogaEtapaDTO {
    @NotBlank(message = "Debe especificar un motivo para justificar la prórroga")
    private String motivoProrroga;

    @NotBlank(message = "Debe especificar la nueva fecha limite")
    @Future(message = "La nueva fecha límite debe ser mayor a la fecha actual")
    private LocalDate nuevaFechaLimite;
}
