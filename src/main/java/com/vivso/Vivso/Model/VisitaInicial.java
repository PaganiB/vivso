package com.vivso.Vivso.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "visitaInicial", schema = "vivso3")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitaInicial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "fecha_visita", nullable = false)
    private LocalDateTime fechaVisita; // Captura exacta para auditoría

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitud", nullable = false)
    private Solicitud solicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tecnico", nullable = false)
    private Usuario tecnico; // Relación con el técnico que realizó el relevamiento

    @Enumerated(EnumType.STRING)
    @Column(name = "clasificacion_preliminar")
    private ClasificacionVivienda clasificacionPreliminar; // 1A, 2A, Derrumbe, etc.

    // Campos GPS separados
    @Column(name = "latitud")
    private Double lat;
    @Column(name = "longitud")
    private Double lng;

    @Column(name = "localidad", length = 100)
    private String localidad;

    @Column(name = "departamento", length = 100)
    private String departamento;

    @Column(columnDefinition = "TEXT")
    private String observaciones; // Para detallar riesgos o situaciones familiares

    // Datos de validación en campo
    private Integer cantHabitantesReal;
    private Boolean tienePrioridadSocial; // Marcador para casos de Chagas, discapacidad o incendio

    //URLs de las fotos
    @ElementCollection
    @CollectionTable(name = "visita_fotos", joinColumns = @JoinColumn(name = "id_visita"))
    @Column(name = "url_foto")
    private List<String> urlsFotos = new ArrayList<>();
}