package com.vivso.Vivso.DTO;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FamiliaDTO {

    private Integer idFamilia;

    private String nombreRepresentante;
    private String contacto;
    private Integer antiguedadRancho;
    private String coordenadasRancho;

    private String cuitOrg;

    private String actaCompromisoUrl;
    private String certificadoResidenciaUrl;
    private String escrituraUrl;
    private String declaracionJuradaUrl;
    private String fotoViviendaUrl;
}
