package com.vivso.Vivso.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificacionDTO {
  private Integer idNotificacion;
  private String titulo;
  private String mensaje;
  private LocalDateTime fecha;
  private Boolean leida;
  private String tipo;
  private Integer idReferencia;
}