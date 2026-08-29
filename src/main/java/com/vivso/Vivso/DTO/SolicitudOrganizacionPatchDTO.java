package com.vivso.Vivso.DTO;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudOrganizacionPatchDTO {

    // --- Datos de la ONG ---
    private String nombre;
    private String tipo;
    private String domLegal;
    private String cpe;
    private LocalDate fechaVencimientoVigencia;
    private LocalDate fechaUltimaAsamblea;

    // --- Datos del Presidente ---
    private String dniPresidente;
    private String nombrePresidente;
    private String apellidoPresidente;
    private String correoPresidente;
    private String telefonoPresidente;
    private LocalDate fechaAltaCargoPresidente;
    private String domicilioPresidente;

    // --- Datos del Tesorero ---
    private String dniTesorero;
    private String nombreTesorero;
    private String apellidoTesorero;
    private String correoTesorero;
    private String telefonoTesorero;
    private LocalDate fechaAltaCargoTesorero;
    private String domicilioTesorero;

    // --- URLs de documentos ---
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
}