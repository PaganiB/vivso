package com.vivso.Vivso.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "familia", schema = "vivso3")
public class Familia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_familia", nullable = false)
    private Integer id;

    @Column(name = "nombreRepresentante", length = 50)
    private String nombreRepresentante;

    @Column(name = "contacto", length = 100)
    private String contacto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CUIT_org", nullable = false)
    private Organizacion cuitOrg;

    @Column(name = "acta_compromiso_url")
    private String actaCompromisoUrl;

    @Column(name = "certificado_residencia_url")
    private String certificadoResidenciaUrl;

    @Column(name = "escritura_url")
    private String escrituraUrl;

    @Column(name = "declaracion_jurada_url")
    private String declaracionJuradaUrl;

    @Column(name = "foto_vivienda_url")
    private String fotoViviendaUrl;

    @Column(name = "antiguedadRancho", columnDefinition = "int CHECK (antiguedadRancho >= 5)")
    private Integer antiguedadRancho;

    @Column(name = "coordenadasRancho", length = 150)
    private String coordenadasRancho;


}