package com.vivso.Vivso.Mapper;

import com.vivso.Vivso.DTO.FamiliaDTO;
import com.vivso.Vivso.DTO.IntegranteFamiliaDTO;
import com.vivso.Vivso.Model.Familia;
import com.vivso.Vivso.Model.IntegranteFamilia;

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
}
