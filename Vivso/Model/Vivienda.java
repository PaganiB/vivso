package com.vivso.Vivso.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "vivienda", schema = "vivso3")
public class Vivienda {
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
    private String estado;

    @Column(name = "observacion", length = 200)
    private String observacion;

}