package com.vivso.Vivso.Mapper;

import com.vivso.Vivso.DTO.FamiliaDTO;
import com.vivso.Vivso.Model.Familia;

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

}
