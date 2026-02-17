package com.vivso.Vivso.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "integrantefamilia", schema = "vivso3")
public class IntegranteFamilia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_integranteFamilia", nullable = false)
    private Integer id;

    @Column(name = "DNI", length = 50)
    private String dni;

    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "apellido", length = 100)
    private String apellido;

    @Column(name = "condicion_especial", length = 100)
    private String condicionEspecial;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_familia", nullable = false)
    private Familia idFamilia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario idUsuario;


}