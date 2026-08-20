package com.vivso.Vivso.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(name = "cpe", length = 100)
    private String cpe;

    @Column(name = "fecha_vencimiento_vigencia", nullable = false)
    private LocalDate fechaVencimientoVigencia; // Constancia de vigencia

    @Column(name = "fecha_ultima_asamblea")
    private LocalDate fechaUltimaAsamblea; // Para rastrear la renovación bianual
}