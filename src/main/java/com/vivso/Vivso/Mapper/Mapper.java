package com.vivso.Vivso.Mapper;

import com.vivso.Vivso.DTO.FamiliaDTO;
import com.vivso.Vivso.DTO.IntegranteFamiliaDTO;
import com.vivso.Vivso.DTO.OrganizacionDTO;
import com.vivso.Vivso.Model.Familia;
import com.vivso.Vivso.Model.IntegranteFamilia;
import com.vivso.Vivso.Model.Organizacion;

public class Mapper {

    //Mapeo de Familia a DTO
    public static FamiliaDTO toDTO(Familia f ) {
        if (f == null) return null;

        return FamiliaDTO.builder()
                .idFamilia(f.getId_familia())
                .nombreRepresentante(f.getNombreRepresentante())
                .contacto(f.getContacto())
                .antiguedadRancho(f.getAntiguedadRancho())
                .coordenadasRancho(f.getCoordenadasRancho())
                .cuitOrg(f.getCuitOrg().getCuit())
                .actaCompromisoUrl(f.getActaCompromisoUrl())
                .certificadoResidenciaUrl(f.getCertificadoResidenciaUrl())
                .escrituraUrl(f.getEscrituraUrl())
                .declaracionJuradaUrl(f.getDeclaracionJuradaUrl())
                .fotoViviendaUrl(f.getFotoViviendaUrl())
                .build();
    }
    //Mapeo de IntegranteFamilia a DTO
    public static IntegranteFamiliaDTO toDTO(IntegranteFamilia iF){
        if (iF == null) return null;

        return IntegranteFamiliaDTO.builder()
                .id(iF.getId_integranteFamilia())
                .nombre(iF.getNombre())
                .apellido(iF.getApellido())
                .condicion_especial(iF.getCondicion_especial() != null ?
                        iF.getCondicion_especial() : "No posee discapacidad")
                .familia(iF.getFamilia().getNombreRepresentante())
                .usuario(iF.getUsuario() != null ? iF.getUsuario().getUsername() : "Sin usuario")
                .build();
    }

    //Mapeo de Organizacion a DTO
    public static OrganizacionDTO toDTO(Organizacion o){
        if (o == null) return null;

        return OrganizacionDTO.builder()
                .cuit(o.getCuit())
                .nombre(o.getNombre())
                .tipo(o.getTipo())
                .dom_legal(o.getDom_legal())
                .contacto(o.getContacto())
                .cpe(o.getCpe())
                .nota_solicitud_url(o.getNota_solicitud_url())
                .vigencia_url(o.getVigencia_url())
                .acta_compromiso_url(o.getActa_compromiso_url())
                .acta_asamblea_url(o.getActa_asamblea_url())
                .dni_autoridades_url(o.getDni_autoridades_url())
                .certificado_residencia_url(o.getCertificado_residencia_url())
                .cuenta_bancaria_url(o.getCuenta_bancaria_url())
                .alta_afip_url(o.getAlta_afip_url())
                .build();
    }

}
