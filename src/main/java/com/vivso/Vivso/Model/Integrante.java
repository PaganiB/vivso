package com.vivso.Vivso.Model;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "integrante", schema = "vivso3")
public class Integrante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_integranteOrganizacion", nullable = false)
    private Integer id_integranteOrganizacion;

    @Column(name = "DNI", length = 50)
    private String dni;

    @Column(name = "nombre", length = 50)
    private String nombre;

    @Column(name = "apellido", length = 50)
    private String apellido;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "funcion", length = 50)
    private String funcion;

    @Column(name = "domicilio", length = 100)
    private String domicilio;

    @Column(name = "activo")
    private Boolean activo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CUIT_org", nullable = false)
    private Organizacion organizacion;


}