package com.vivso.Vivso.Model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "familiar", schema = "vivso3")
public class Familiar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_integranteFamilia", nullable = false)
    private Integer idIntegranteFamilia;

    @Column(name = "DNI", length = 50)
    private String dni;

    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "apellido", length = 100)
    private String apellido;

    // 1. Esto asegura que en Java nunca sea null al usar Builder
    @Builder.Default
    // 2. Esto asegura que en la DB la columna tenga el valor por defecto
    @Column(name = "condicion_especial", length = 100, nullable = false, columnDefinition = "varchar(255) default 'No posee discapacidad'")
    private String condicion_especial = "No posee discapacidad";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_familia", nullable = false)
    private Familia familia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;


}