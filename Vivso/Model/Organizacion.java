package com.vivso.Vivso.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "organizacion", schema = "vivso3")
public class Organizacion {
    @Id
    @Column(name = "CUIT", nullable = false, length = 13)
    private String cuit;

    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "tipo", length = 100)
    private String tipo;

    @Column(name = "dom_legal", length = 100)
    private String domLegal;

    @Column(name = "contacto", length = 100)
    private String contacto;

    @Column(name = "cpe", length = 100)
    private String cpe;

    @Column(name = "nota_solicitud_url")
    private String notaSolicitudUrl;

    @Column(name = "vigencia_url")
    private String vigenciaUrl;

    @Column(name = "acta_compromiso_url")
    private String actaCompromisoUrl;

    @Column(name = "acta_asamblea_url")
    private String actaAsambleaUrl;

    @Column(name = "dni_autoridades_url")
    private String dniAutoridadesUrl;

    @Column(name = "certificado_residencia_url")
    private String certificadoResidenciaUrl;

    @Column(name = "cuenta_bancaria_url")
    private String cuentaBancariaUrl;

    @Column(name = "alta_afip_url")
    private String altaAfipUrl;


}