package com.vivso.Vivso.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RechazarEtapaDTO {
  @NotBlank(message = "El motivo del rechazo es obligatorio")
  private String motivoRechazo;
}