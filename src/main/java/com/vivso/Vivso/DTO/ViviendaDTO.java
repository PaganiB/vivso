package com.vivso.Vivso.DTO;

import com.vivso.Vivso.Model.TipoVivienda;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViviendaDTO {

  private Integer idVivienda;

  private String departamento;
  private String localidad;
  private String barrio;
  private String direccion;

  private BigDecimal superficie;

  private LocalDate fechaInic;
  private LocalDate fechaFin;

  private Integer idFamilia;
  private String representante;

  private Double lat;
  private Double lng;

  private Integer avanceObra;

  private TipoVivienda tipoVivienda;

  private Integer cantDormitorios;

  private Integer solicitud;
}