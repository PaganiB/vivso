package com.vivso.Vivso.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "visitaObra")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitaObra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_visita_obra")
    private Integer idVisitaObra;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private Integer afo; // Avance Físico de Obra (0 a 100)

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @Column(length = 500)
    private String observaciones;

    // --- Relaciones ---

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vivienda", nullable = false)
    private Vivienda vivienda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_tecnico", nullable = false)
    private Usuario tecnico;
}