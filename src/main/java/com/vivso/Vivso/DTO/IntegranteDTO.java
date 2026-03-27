package com.vivso.Vivso.DTO;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IntegranteDTO {

    private Integer idIntegrante;

    private String dni;
    private String nombre;
    private String apellido;
    private String telefono;
    private String funcion;
    private String domicilio;
    private Boolean activo;

    private String usuario;
    private String cuitOrg;

}
