package com.vivso.Vivso.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

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

    // --- 4. RUTAS FÍSICAS DE LOS DOCUMENTOS ---
    private String urlNotaSolicitud;
    private String urlConstanciaVigencia;
    private String urlActaCompromiso;
    private String urlActaAsamblea;
    private String urlConstanciaCuentaBancaria;
    private String urlAltaAfip;
    private String urlDniPresidente;
    private String urlDniTesorero;
    private String urlCertificadoResidenciaPresidente;
    private String urlCertificadoResidenciaTesorero;

    // --- 5. CONTROL Y AUDITORÍA ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado;

    @Column(length = 500)
    private String motivo;

    private LocalDate fechaRechazo;
    private LocalDate fechaAprobacion;
    private LocalDate fechaSolicitud;

    @PrePersist
    public void prePersist() {
        if (this.estado == null) {
            this.estado = EstadoSolicitud.Pendiente;
        }
        if (this.fechaSolicitud == null) {
            this.fechaSolicitud = LocalDate.now();
        }
    }
}