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

}