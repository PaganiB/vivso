package com.vivso.Vivso.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "familia", schema = "vivso3")
public class Familia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_familia", nullable = false)
    private Integer id_familia;

    @Column(name = "nombreRepresentante", length = 50)
    private String nombreRepresentante;

    @Column(name = "contacto", length = 100)
    private String contacto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CUIT_org", nullable = false)
    private Organizacion cuitOrg;

    @Column(name = "antiguedadRancho", columnDefinition = "int CHECK (antiguedadRancho >= 5)")
    private Integer antiguedadRancho;

    @Column(name = "coordenadasRancho", length = 150)
    private String coordenadasRancho;
}