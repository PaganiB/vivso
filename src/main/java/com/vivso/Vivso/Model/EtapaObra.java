package com.vivso.Vivso.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "etapa_obra", uniqueConstraints = @UniqueConstraint(columnNames = {"id_solicitud", "etapa"}))
public class EtapaObra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_etapa_obra", nullable = false)
    private Integer idEtapaObra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_solicitud", nullable = false)
    private Solicitud solicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa", nullable = false, length = 20)
    private EtapaConstruccion etapa;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoEtapa estado;

    // Se completan al habilitar la etapa (pasa a EN_CURSO), no antes.
    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    // Vencimiento vigente: arranca como fechaInicio + diasEstimados y el técnico puede prorrogarlo.
    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;

    // Vencimiento original, no se modifica nunca (tope y auditoría de la prórroga).
    @Column(name = "fecha_limite_original")
    private LocalDate fechaLimiteOriginal;

    @Column(name = "motivo_prorroga", length = 200)
    private String motivoProrroga;

    // Cuando la ONG la envía a revisión.
    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "observacion_organizacion", length = 500)
    private String observacionOrganizacion;

    // Integrante de la ONG (su usuario) que envió la etapa a revisión.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_envio")
    private Usuario enviadoPor;

    // Revisión del técnico.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_revisor")
    private Usuario revisadoPor;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    @Column(name = "observacion_tecnico", length = 500)
    private String observacionTecnico;

    @PrePersist
    public void prePersist() {
        if (this.estado == null) {
            this.estado = EstadoEtapa.PENDIENTE;
        }
    }

}

