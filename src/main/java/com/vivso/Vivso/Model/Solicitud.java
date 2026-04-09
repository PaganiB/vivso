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
    private Integer id_solicitud;

    @Column(name = "GDE", length = 50)
    private String GDE;

    @Column(name = "fechaSolicitud")
    private LocalDate fechaSolicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CUIT_org", nullable = false)
    private Organizacion cuitOrg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "numExp")
    private Vivienda numExp;

    @Lob
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoSolicitud estado;

    @Column(name = "observacion", length = 200)
    private String observacion;


}