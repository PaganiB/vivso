package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.FamiliaDTO;
import com.vivso.Vivso.Mapper.Mapper;
import com.vivso.Vivso.Model.Familia;
import com.vivso.Vivso.Model.Organizacion;
import com.vivso.Vivso.Repository.IFamiliaRepository;
import com.vivso.Vivso.Repository.IOrganizacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FamiliaService implements IFamiliaService {

    @Autowired
    private IFamiliaRepository familiaRepo;
    @Autowired
    private IOrganizacionRepository orgRepo;

    @Override
    public List<FamiliaDTO> getFamilias() {
        //Creamos listas para familias y dto
        List<Familia> familias = familiaRepo.findAll();
        List<FamiliaDTO> familiasDto = new ArrayList<>();
        //Recorremos todas las familias de la lista familia
        //Las convertimos en dto y las agregamos a la lista dto
        FamiliaDTO dto;
        for (Familia f : familias) {
            dto = Mapper.toDTO(f);
            familiasDto.add (dto);
        }
        return familiasDto;
    }

    @Override
    public FamiliaDTO saveFamilia(FamiliaDTO familiaDto) {
        //Validaciones
        if (familiaDto.getCuitOrg() == null) throw new RuntimeException("Debe indicar el cuit");
        //Buscar la organización
        Organizacion org= orgRepo.findById(familiaDto.getCuitOrg()).orElse(null);
        if (org == null) {
            throw new RuntimeException("el Cuit no existe");
        }
        //Construir nuestro objeto familia
        var fam = Familia.builder()
                .nombreRepresentante(familiaDto.getNombreRepresentante())
                .contacto(familiaDto.getContacto())
                .cuitOrg(org)
                .actaCompromisoUrl(familiaDto.getActaCompromisoUrl())
                .certificadoResidenciaUrl(familiaDto.getCertificadoResidenciaUrl())
                .escrituraUrl(familiaDto.getEscrituraUrl())
                .declaracionJuradaUrl(familiaDto.getDeclaracionJuradaUrl())
                .fotoViviendaUrl(familiaDto.getFotoViviendaUrl())
                .antiguedadRancho(familiaDto.getAntiguedadRancho())
                .coordenadasRancho(familiaDto.getCoordenadasRancho())
                .build();
        //Guardamos en la BD
        familiaRepo.save(fam);
        //Mapeo de salida
        FamiliaDTO familiaSalida = Mapper.toDTO(fam);
        return familiaSalida;
    }

    @Override
    public FamiliaDTO updateFamilia(Integer idFamilia, FamiliaDTO familiaDto) {
        //buscar si la familia existe para actualizarla
        Familia f = familiaRepo.findById(idFamilia).orElse(null);
        if (f == null) throw new RuntimeException("familia no encontrada");
        //hacemos el update en todos los atributos donde sea necesario
        if (familiaDto.getNombreRepresentante()!=null) {
            f.setNombreRepresentante(familiaDto.getNombreRepresentante());
        }
        if (familiaDto.getContacto()!=null) {
            f.setContacto(familiaDto.getContacto());
        }
        if (familiaDto.getAntiguedadRancho()!=null) {
            f.setAntiguedadRancho(familiaDto.getAntiguedadRancho());
        }
        if (familiaDto.getCoordenadasRancho()!=null) {
            f.setCoordenadasRancho(familiaDto.getCoordenadasRancho());
        }
        if (familiaDto.getCuitOrg()!=null) {
            Organizacion org = orgRepo.findById(familiaDto.getCuitOrg()).orElse(null);
            if (org == null) throw new RuntimeException("Organizacion no encontrada");
            f.setCuitOrg(org);
        }
        if (familiaDto.getActaCompromisoUrl()!=null) {
            f.setActaCompromisoUrl(familiaDto.getActaCompromisoUrl());
        }
        if (familiaDto.getCertificadoResidenciaUrl()!=null) {
            f.setCertificadoResidenciaUrl(familiaDto.getCertificadoResidenciaUrl());
        }
        if (familiaDto.getEscrituraUrl()!=null) {
            f.setEscrituraUrl(familiaDto.getEscrituraUrl());
        }
        if (familiaDto.getDeclaracionJuradaUrl()!=null) {
            f.setDeclaracionJuradaUrl(familiaDto.getDeclaracionJuradaUrl());
        }
        if (familiaDto.getFotoViviendaUrl()!=null) {
            f.setFotoViviendaUrl(familiaDto.getFotoViviendaUrl());
        }
        //Guardamos en la BD la familia actualizada
        familiaRepo.save(f);
        //Mapeo de la salida
        FamiliaDTO familiaSalida = Mapper.toDTO(f);
        return familiaSalida;
    }

    @Override
    public void deleteFamilia(Integer id) {
        Familia f = familiaRepo.findById(id).orElse(null);
        if (f == null) throw new RuntimeException("Familia no encontrada");
        familiaRepo.delete(f);
    }

    @Override
    public FamiliaDTO findFamilia(Integer id) {
        //Buscamos la familia
        Familia f = familiaRepo.findById(id).orElse(null);
        if (f == null) throw new RuntimeException("Familia no encontrada");
        //Convertivos familia a Dto
        return Mapper.toDTO(f);
    }

}
