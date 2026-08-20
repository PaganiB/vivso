package com.vivso.Vivso.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "solicitudOrganizacion", schema = "vivso3")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudOrganizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud_org")
    private Integer idSolicitudOrg; // PK autoincremental, no es el CUIT

    // --- DATOS DE LA ONG EN ESPERA ---
    @Column(nullable = false)
    private String cuit; // Se guarda como texto normal hasta que se apruebe

    @Column(nullable = false)
    private String nombre;

    private String domLegal;
    private String contacto;
    private String cpe;
    private LocalDate fechaVencimientoVigencia;
    private LocalDate fechaUltimaAsamblea;

    // --- LOS 4 ATRIBUTOS DE CONTROL ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado;

    @Column(length = 500)
    private String motivo; // Por qué se rechazó (ej: "Constancia AFIP ilegible")

    private LocalDate fechaRechazo;
    private LocalDate fechaAprobacion;
    private LocalDate fechaSolicitud;

    @PrePersist
    public void prePersist() {
        if (this.estado == null) {
            this.estado = EstadoSolicitud.Pendiente;
        }
    }
}