package com.vivso.Vivso.Model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "documento")
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // Habilita el "escucha" de auditoría
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDocumento;

    @Column(name = "nombreOriginal", nullable = false)
    private String nombre;

    @Column(name = "urlPath", nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private TipoDocumento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDocumento estado;

    @Column(length = 500)
    private String motivoRechazo;

    // --- Auditoría Automática ---

    @CreatedDate
    @Column(name = "fechaSubida",updatable = false, nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime fechaSubida;

    @Column(name = "fechaRevision")
    private LocalDateTime fechaRevision;

    // --- Relaciones ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_familia")
    private Familia familia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_organizacion")
    private Organizacion organizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_revisor")
    private Usuario revisor; // El administrativo que lo aprobó o rechazó

    // Metodo para inicializar el estado por defecto si viene nulo
    @PrePersist
    public void prePersist() {
        if (this.estado == null) {
            this.estado = EstadoDocumento.PENDIENTE;
        }
    }
}
