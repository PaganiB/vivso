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
@Table(name = "vivienda", schema = "vivso3")
public class Vivienda{
    @Id
    @Column(name = "numExp", nullable = false, length = 50)
    private String numExp;

    @Column(name = "departamento", length = 100)
    private String departamento;

    @Column(name = "localidad", length = 100)
    private String localidad;

    @Column(name = "direccion", length = 100)
    private String direccion;

    @Column(name = "superficie", precision = 10, scale = 2)
    private BigDecimal superficie;

    @Column(name = "fechaInic")
    private LocalDate fechaInic;

    @Column(name = "fechaFin")
    private LocalDate fechaFin;

    @Lob
    @Column(name = "estado")
    @Enumerated(EnumType.STRING)
    private EstadoVivienda estado;

    @Column(name = "observacion", length = 200)
    private String observacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_familia", nullable = false)
    private Familia familia;
}