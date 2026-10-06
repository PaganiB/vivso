package com.vivso.Vivso.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

// Entrada: el técnico fija un nuevo vencimiento. El tope (fechaLimiteOriginal + 14 días),
// que sea posterior al vencimiento vigente y día hábil se validan en el service.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProrrogaEtapaDTO {

    @NotNull(message = "La nueva fecha de vencimiento es obligatoria")
    @FutureOrPresent(message = "La nueva fecha de vencimiento no puede estar en el pasado")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nuevaFechaLimite;

    @NotBlank(message = "Debe indicar el motivo de la prórroga")
    @Size(max = 200, message = "El motivo no puede superar los 200 caracteres")
    private String motivoProrroga;
}