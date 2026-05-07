package com.vivso.Vivso.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "visita", schema = "vivso3")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Visita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_visita", nullable = false)
    private LocalDateTime fechaVisita; // Captura exacta para auditoría

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vivienda", nullable = false)
    private Vivienda vivienda; // Relación con la casa visitada

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tecnico", nullable = false)
    private Usuario tecnico; // Relación con el técnico que realizó el relevamiento

    @Column(name = "avance_registrado")
    private Integer avanceRegistrado; // El AFO (0-100) en ese momento específico

    @Enumerated(EnumType.STRING)
    @Column(name = "clasificacion_preliminar")
    private ClasificacionVivienda clasificacionPreliminar; // 1A, 2A, Derrumbe, etc.

    // Campos GPS separados
    @Column(name = "latitud")
    private Double lat;
    @Column(name = "longitud")
    private Double lng;

    @Column(columnDefinition = "TEXT")
    private String observaciones; // Para detallar riesgos o situaciones familiares

    // Datos de validación en campo
    private Integer cantHabitantesReal;
    private Boolean tienePrioridadSocial; // Marcador para casos de Chagas, discapacidad o incendio

}
