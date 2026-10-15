package com.vivso.Vivso.Model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "vivienda")
public class Vivienda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vivienda", nullable = false)
    private Integer idVivienda;

    @Column(name = "departamento", length = 100)
    private String departamento;

    @Column(name = "localidad", length = 100)
    private String localidad;

    @Column(name = "direccion", length = 100)
    private String direccion;

    @Column(name = "barrio ", length = 100)
    private String barrio;

    @Column(name = "superficie", precision = 10, scale = 2)
    private BigDecimal superficie;

    @Column(name = "fechaInic")
    private LocalDate fechaInic;

    @Column(name = "fechaFin")
    private LocalDate fechaFin;

    @Column(name = "latitud")
    private Double lat;
    @Column(name = "longitud")
    private Double lng;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_familia", nullable = false)
    private Familia familia;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_solicitud", nullable = false, unique = true)
    private Solicitud solicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipoVivienda")
    private TipoVivienda tipoVivienda;

    @Column(name = "cant_dormitorios")
    private Integer cantDormitorios;
}