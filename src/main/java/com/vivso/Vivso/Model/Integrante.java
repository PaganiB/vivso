package com.vivso.Vivso.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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
    private Integer idIntegrante;

    @Column(name = "DNI", length = 50)
    private String dni;

    @Column(name = "nombre", length = 50)
    private String nombre;

    @Column(name = "apellido", length = 50)
    private String apellido;

    @Column(name = "correo", length = 250)
    private String correo;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "cargo", length = 50)
    private TipoCargo cargo;

    @Column(name = "domicilio", length = 100)
    private String domicilio;

    @Column(name = "activo")
    private Boolean activo;

    // Nuevo campo para controlar el vencimiento de la autoridad (2 años)
    @Column(name = "fecha_alta_cargo")
    private LocalDate fechaAltaCargo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CUIT_org", nullable = false)
    private Organizacion organizacion;


}