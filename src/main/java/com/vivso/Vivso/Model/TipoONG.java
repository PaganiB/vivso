package com.vivso.Vivso.Model;

import lombok.Getter;

@Getter
public enum TipoONG {
  ASOCIACION_CIVIL("Asociación Civil"),
  FUNDACION("Fundación"),
  COOPERATIVA("Cooperativa"),
  COMUNIDAD_INDIGENA("Comunidad Indígena"),
  INSTITUCION_RELIGIOSA("Institución Religiosa"),
  CLUB_SOCIAL_Y_DEPORTIVO("Club Social y Deportivo"),
  MUTUAL("Mutual"),
  OTRA("Otra");

  private final String descripcion;

  TipoONG(String descripcion) {
    this.descripcion = descripcion;
  }
}