
package com.vivso.Vivso.Model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "familia")
public class Familia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_familia", nullable = false)
    private Integer idFamilia;

    @Column(name = "nombreRepresentante", length = 50)
    private String nombreRepresentante;

    @Column(name = "dniRepresentante", length = 8)
    private String dniRepresentante;

    @Column(name = "contacto", length = 100)
    private String contacto;

    @Column(name = "antiguedadRancho")
    private Integer antiguedadRancho;

    @Column(name = "referencia_ubicacion", length = 255)
    private String referenciaUbicacion;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;
}
