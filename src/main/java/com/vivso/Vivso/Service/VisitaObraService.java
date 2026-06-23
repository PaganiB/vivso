package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.VisitaObraDTO;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Model.VisitaObra;
import com.vivso.Vivso.Model.Vivienda;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import com.vivso.Vivso.Repository.IVisitaObraRepository;
import com.vivso.Vivso.Repository.IViviendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisitaObraService implements IVisitaObraService {

    private  IVisitaObraRepository visitaRepo;
    private  IViviendaRepository viviendaRepo;
    private  IUsuarioRepository usuarioRepo;
    private VivsoMapper mapper;

    @Override
    @Transactional
    public VisitaObraDTO registrarVisita(VisitaObraDTO dto) {

        Vivienda vivienda = viviendaRepo.findById(dto.getNumExpVivienda())
                .orElseThrow(() -> new RuntimeException("Error: Vivienda no encontrada con expediente " + dto.getNumExpVivienda()));

        Usuario tecnico = usuarioRepo.findById(dto.getIdTecnico())
                .orElseThrow(() -> new RuntimeException("Error: Técnico no encontrado con ID " + dto.getIdTecnico()));

        VisitaObra visita = mapper.toEntity(dto);

        visita.setVivienda(vivienda);
        visita.setTecnico(tecnico);

        vivienda.setAvanceObra(dto.getAfo());
        viviendaRepo.save(vivienda);

        VisitaObra visitaGuardada = visitaRepo.save(visita);
        return mapper.toDTO(visitaGuardada);
    }

    @Override
    public List<VisitaObraDTO> obtenerHistorialPorVivienda(String numExp) {
        return visitaRepo.findByVivienda_NumExpOrderByFechaDesc(numExp)
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
}