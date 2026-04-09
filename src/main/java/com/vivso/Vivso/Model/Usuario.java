package com.vivso.Vivso.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usuario", schema = "vivso3")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario", nullable = false)
    private Integer id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String password_hash;

    @Lob
    @Column(name = "rol", nullable = false)
    private String rol;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "activo")
    private Boolean activo;

}