package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.ViviendaDTO;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.EstadoVivienda;
import com.vivso.Vivso.Model.Familia;
import com.vivso.Vivso.Model.Vivienda;
import com.vivso.Vivso.Repository.IFamiliaRepository;
import com.vivso.Vivso.Repository.IViviendaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ViviendaService implements IViviendaService {

    @Autowired private IViviendaRepository viviendaRepo;
    @Autowired private IFamiliaRepository familiaRepo;
    @Autowired private VivsoMapper mapper;

    @Override
    public List<ViviendaDTO> listarTodas() {
        return viviendaRepo.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public ViviendaDTO buscarPorExpediente(String numExp) {
        return viviendaRepo.findViviendaByNumExp(numExp)
                .map(mapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Vivienda no encontrada: " + numExp));
    }

    @Override
    @Transactional
    public void eliminar(String numExp) {
        if (!viviendaRepo.existsById(numExp))
            throw new RuntimeException("No se puede eliminar: la vivienda " + numExp + " no existe.");
        viviendaRepo.deleteById(numExp);
    }

    @Override
    public List<ViviendaDTO> buscarPorEstado(EstadoVivienda estado) {
        return viviendaRepo.findViviendaByEstado(estado).stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public Optional<ViviendaDTO> buscarPorFamilia(Integer idFamilia) {
        return viviendaRepo.findByFamilia_IdFamilia(idFamilia)
                .map(mapper::toDTO);
    }

    @Override
    @Transactional
    public ViviendaDTO guardar(ViviendaDTO dto) {
        Familia fam = familiaRepo.findById(dto.getIdFamilia())
                .orElseThrow(() -> new RuntimeException("Familia no encontrada: " + dto.getIdFamilia()));

        Vivienda v = mapper.toEntity(dto);
        v.setFamilia(fam);
        if (v.getAvanceObra() == null) v.setAvanceObra(0);

        return mapper.toDTO(viviendaRepo.saveAndFlush(v));
    }

    @Override
    @Transactional
    public ViviendaDTO actualizar(String numExp, ViviendaDTO dto) {
        Vivienda v = viviendaRepo.findById(numExp)
                .orElseThrow(() -> new RuntimeException("Vivienda no encontrada: " + numExp));

        mapper.updateFromDto(dto, v);

        if (dto.getIdFamilia() != null) {
            v.setFamilia(familiaRepo.findById(dto.getIdFamilia())
                    .orElseThrow(() -> new RuntimeException("Familia no encontrada: " + dto.getIdFamilia())));
        }

        return mapper.toDTO(viviendaRepo.saveAndFlush(v));
    }

    @Override
    public List<ViviendaDTO> filtrarPorLocalidad(String localidad) {
        return viviendaRepo.findByLocalidadContainingIgnoreCase(localidad).stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<ViviendaDTO> filtrarPorAnioInicio(int anio) {
        return viviendaRepo.findByAnioInicio(anio).stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<ViviendaDTO> filtrarPorAnioFin(int anio) {
        return viviendaRepo.findByAnioFin(anio).stream()
                .map(mapper::toDTO)
                .toList();
    }
}
