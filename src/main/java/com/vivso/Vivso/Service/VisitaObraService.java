package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.VisitaObraDTO;
import com.vivso.Vivso.Exception.RecursoNoEncontradoException;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Model.VisitaObra;
import com.vivso.Vivso.Model.Vivienda;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import com.vivso.Vivso.Repository.IVisitaObraRepository;
import com.vivso.Vivso.Repository.IViviendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VisitaObraService implements IVisitaObraService {

    @Autowired IVisitaObraRepository visitaRepo;
    @Autowired  IViviendaRepository viviendaRepo;
    @Autowired  IUsuarioRepository usuarioRepo;
    @Autowired  VivsoMapper mapper;

    @Override
    @Transactional
    public VisitaObraDTO registrarVisita(VisitaObraDTO dto) {

        Vivienda vivienda = viviendaRepo.findById(dto.getIdVivienda())
                .orElseThrow(() -> new RecursoNoEncontradoException("Vivienda no encontrada con ID " + dto.getIdVivienda()));

        Usuario tecnico = usuarioRepo.findById(dto.getIdTecnico())
                .orElseThrow(() -> new RecursoNoEncontradoException("Técnico no encontrado con ID " + dto.getIdTecnico()));

        VisitaObra visita = mapper.toEntity(dto);

        visita.setVivienda(vivienda);
        visita.setTecnico(tecnico);

        vivienda.setAvanceObra(dto.getAfo());
        viviendaRepo.save(vivienda);

        VisitaObra visitaGuardada = visitaRepo.save(visita);
        return mapper.toDTO(visitaGuardada);
    }

    @Override
    public List<VisitaObraDTO> obtenerHistorialPorVivienda(Integer idVivienda) {
        return visitaRepo.findByVivienda_IdViviendaOrderByFechaDesc(idVivienda)
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
}