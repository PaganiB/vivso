package com.vivso.Vivso.DTO;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrganizacionDTO {

    private String cuit;

    private String nombre;
    private String tipo;
    private String dom_legal;
    private String contacto;
    private String cpe;

    private String nota_solicitud_url;
    private String vigencia_url;
    private String acta_compromiso_url;
    private String acta_asamblea_url;
    private String dni_autoridades_url;
    private String certificado_residencia_url;
    private String cuenta_bancaria_url;
    private String alta_afip_url;

}
