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

    @NotNull(message = "Debe especificar la nueva fecha límite")
    @Future(message = "La nueva fecha límite debe ser un día en el futuro")
    private LocalDate nuevaFechaLimite;

    @NotBlank(message = "Debe especificar un motivo para justificar la prórroga")
    private String motivoProrroga;
}
