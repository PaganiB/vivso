package com.vivso.Vivso.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "solicitud", schema = "vivso3")
public class Solicitud {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud", nullable = false)
    private Integer idSolicitud;

    @Column(name = "fechaSolicitud")
    private LocalDate fechaSolicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CUIT_org", nullable = false)
    private Organizacion cuitOrg;

    @Column(name = "numExp")
    private String numExp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vivienda")
    private Vivienda vivienda;

    @Lob
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoSolicitud estado;

    @Column(name = "observacion", length = 200)
    private String observacion;

    @Column(name = "fechaActivacion")
    private LocalDate fechaActivacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_familia", nullable = false)
    private Familia familiaBeneficiaria;

    @PrePersist
    public void prePersist() {
        if (this.estado == null) {
            this.estado = EstadoSolicitud.Pendiente;
        }
        if (this.fechaSolicitud == null) {
            this.fechaSolicitud = LocalDate.now();
        }
    }
}