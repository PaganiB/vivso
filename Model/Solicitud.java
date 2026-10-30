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
@Table(name = "solicitud")
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

    @Lob
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoSolicitud estado;

    @Column(name = "observacion", length = 200)
    private String observacion;

    @Column(name = "camposObservados")
    private String camposObservados;

    @Column(name = "fechaActivacion")
    private LocalDate fechaActivacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_familia", nullable = false)
    private Familia familiaBeneficiaria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_revisor")
    private Usuario revisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tecnico_asignado")
    private Usuario tecnicoAsignado;

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