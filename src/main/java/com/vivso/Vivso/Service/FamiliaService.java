package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.FamiliaDTO;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.Familia;
import com.vivso.Vivso.Model.Organizacion;
import com.vivso.Vivso.Repository.IFamiliaRepository;
import com.vivso.Vivso.Repository.IOrganizacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FamiliaService implements IFamiliaService {

    @Autowired private IFamiliaRepository familiaRepo;
    @Autowired private IOrganizacionRepository orgRepo;
    @Autowired private VivsoMapper mapper;

    @Override
    public List<FamiliaDTO> getFamilias() {
        return familiaRepo.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public FamiliaDTO saveFamilia(FamiliaDTO dto) {
        Familia familia = mapper.toEntity(dto);

        return mapper.toDTO(familiaRepo.save(familia));
    }

    @Override
    public FamiliaDTO updateFamilia(Integer idFamilia, FamiliaDTO dto) {
        Familia f = familiaRepo.findById(idFamilia)
                .orElseThrow(() -> new RuntimeException("Familia no encontrada: " + idFamilia));

        // MapStruct aplica solo los campos no nulos del DTO sobre la entidad existente
        mapper.updateFromDto(dto, f);

        return mapper.toDTO(familiaRepo.save(f));
    }

    @Override
    public void deleteFamilia(Integer id) {
        Familia f = familiaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Familia no encontrada: " + id));
        familiaRepo.delete(f);
    }

    @Override
    public FamiliaDTO findFamilia(Integer id) {
        return familiaRepo.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Familia no encontrada: " + id));
    }
}
