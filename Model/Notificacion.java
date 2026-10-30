package com.vivso.Vivso.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
@Entity
@Table(name = "notificacion")
public class Notificacion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_notificacion", nullable = false)
  private Integer idNotificacion;

  @Column(name = "titulo", nullable = false, length = 100)
  private String titulo;

  @Column(name = "mensaje", nullable = false, length = 255)
  private String mensaje;

  @Column(name = "fecha", nullable = false)
  private LocalDateTime fecha;

  @Column(name = "leida", nullable = false)
  private Boolean leida;

  @Column(name = "tipo", length = 50)
  private String tipo; // Ej: ETAPA_HABILITADA, ETAPA_APROBADA, ETAPA_OBSERVADA, etc.

  @Column(name = "id_referencia")
  private Integer idReferencia; // ID de la solicitud para que el celular sepa a dónde navegar

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_usuario", nullable = false)
  private Usuario usuario;

  @PrePersist
  public void prePersist() {
    if (this.fecha == null) {
      this.fecha = LocalDateTime.now();
    }
    if (this.leida == null) {
      this.leida = false;
    }
  }
}