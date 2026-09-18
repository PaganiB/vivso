package com.vivso.Vivso.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "solicitud_organizacion", schema = "vivso3")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudOrganizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud_org")
    private Integer idSolicitudOrg;

    // --- 1. DATOS DE LA ONG ---
    @Column(nullable = false, unique = true)
    private String cuit;
    private String nombre;
    private String tipo;
    private String domLegal;
    private String cpe;
    private LocalDate fechaVencimientoVigencia;
    private LocalDate fechaUltimaAsamblea;

    // --- 2. DATOS DEL PRESIDENTE ---
    private String dniPresidente;
    private String nombrePresidente;
    private String apellidoPresidente;
    private String correoPresidente;
    private String telefonoPresidente;
    private LocalDate fechaAltaCargoPresidente;
    private String domicilioPresidente;

    // --- 3. DATOS DEL TESORERO (Opcional) ---
    private String dniTesorero;
    private String nombreTesorero;
    private String apellidoTesorero;
    private String correoTesorero;
    private String telefonoTesorero;
    private LocalDate fechaAltaCargoTesorero;
    private String domicilioTesorero;

    // --- 5. CONTROL Y AUDITORÍA ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado;

    @Column(length = 500)
    private String motivo;

    private LocalDate fechaRechazo;
    private LocalDate fechaAprobacion;
    private LocalDate fechaSolicitud;

    @Column(name = "token_edicion", unique = true)
    private String tokenEdicion;

    @Column(name = "campos_observados", columnDefinition = "TEXT")
    private String camposObservados;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_revisor")
    private Usuario revisor;

    @PrePersist
    public void prePersist() {
        if (this.estado == null) {
            this.estado = EstadoSolicitud.Pendiente;
        }
        if (this.fechaSolicitud == null) {
            this.fechaSolicitud = LocalDate.now();
        }
        if (this.tokenEdicion == null) {
            this.tokenEdicion = UUID.randomUUID().toString();
        }
    }
}