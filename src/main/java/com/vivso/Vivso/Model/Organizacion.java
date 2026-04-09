package com.vivso.Vivso.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
    private String dom_legal;

    @Column(name = "contacto", length = 100)
    private String contacto;

    @Column(name = "cpe", length = 100)
    private String cpe;

    @Column(name = "nota_solicitud_url")
    private String nota_solicitud_url;

    @Column(name = "vigencia_url")
    private String vigencia_url;

    @Column(name = "acta_compromiso_url")
    private String acta_compromiso_url;

    @Column(name = "acta_asamblea_url")
    private String acta_asamblea_url;

    @Column(name = "dni_autoridades_url")
    private String dni_autoridades_url;

    @Column(name = "certificado_residencia_url")
    private String certificado_residencia_url;

    @Column(name = "cuenta_bancaria_url")
    private String cuenta_bancaria_url;

    @Column(name = "alta_afip_url")
    private String alta_afip_url;

}