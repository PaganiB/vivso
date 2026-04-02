package com.vivso.Vivso.Mapper;

import com.vivso.Vivso.DTO.*;
import com.vivso.Vivso.Model.*;

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
    //Mapeo de Familiar a DTO
    public static FamiliarDTO toDTO(Familiar iF){
        if (iF == null) return null;

        return FamiliarDTO.builder()
                .idIntegranteFamilia(iF.getIdIntegranteFamilia())
                .dni(iF.getDni())
                .nombre(iF.getNombre())
                .apellido(iF.getApellido())
                .condicion_especial(iF.getCondicion_especial() != null ?
                        iF.getCondicion_especial() : "No posee discapacidad")
                .familia(iF.getFamilia().getId_familia())
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

    //Mapeo de Integrante a DTO
    public static IntegranteDTO toDTO(Integrante iO){
        if (iO == null) return null;

        return IntegranteDTO.builder()
                .idIntegrante(iO.getId_integranteOrganizacion())
                .dni(iO.getDni())
                .nombre(iO.getNombre())
                .apellido(iO.getApellido())
                .telefono(iO.getTelefono())
                .funcion(iO.getFuncion())
                .domicilio(iO.getDomicilio())
                .activo(iO.getActivo())
                .cuitOrg(iO.getOrganizacion().getCuit())
                .usuario(iO.getUsuario() != null ? iO.getUsuario().getUsername() : "Sin usuario")
                .build();
    }




















    //empiezo

}
