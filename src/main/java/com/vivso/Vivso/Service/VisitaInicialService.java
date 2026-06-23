package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.VisitaInicialDTO;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.Solicitud;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Model.VisitaInicial;
import com.vivso.Vivso.Repository.ISolicitudRepository;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import com.vivso.Vivso.Repository.IVisitaInicialRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class VisitaInicialService implements IVisitaInicialService {

    @Autowired IVisitaInicialRepository visitaRepository;
    @Autowired ISolicitudRepository solicitudRepository;
    @Autowired IUsuarioRepository usuarioRepository;
    @Autowired VivsoMapper vivsoMapper;

    @Override
    public List<VisitaInicialDTO> getVisitas() {
        return visitaRepository.findAll().stream()
                .map(vivsoMapper::toDTO)
                .toList();
    }

    @Override
    public VisitaInicialDTO saveVisita(VisitaInicialDTO dto) {
        Usuario tecnico = usuarioRepository.findById(dto.getIdTecnico())
                .orElseThrow(() -> new RuntimeException("Usuario tecnico no encontrado con id " + dto.getIdTecnico()));

        Solicitud solicitud = solicitudRepository.findById(dto.getIdSolicitud())
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con id " + dto.getIdSolicitud()));

        VisitaInicial v = vivsoMapper.toEntity(dto);
        v.setTecnico(tecnico);
        v.setSolicitud(solicitud);

        return vivsoMapper.toDTO(visitaRepository.save(v));
    }

    @Override
    @Transactional
    public VisitaInicialDTO updateVisita(Integer idVisita, VisitaInicialDTO dto) {
        VisitaInicial v = visitaRepository.findById(idVisita)
                .orElseThrow(() -> new RuntimeException("Visita no encontrada"));

        vivsoMapper.updateFromDto(dto, v);

        v.setTecnico(usuarioRepository.findById(dto.getIdTecnico())
                .orElseThrow(() -> new RuntimeException("Tecnico no encontrado")));

        return vivsoMapper.toDTO(visitaRepository.save(v));
    }

    @Override
    public void deleteVisita(Integer idVisita) {
        VisitaInicial v = visitaRepository.findById(idVisita)
                .orElseThrow(() -> new RuntimeException("Visita no encontrada"));
        visitaRepository.delete(v);
    }

    @Override
    public VisitaInicialDTO FindVisita(Integer idVisita) {
        VisitaInicial v = visitaRepository.findById(idVisita)
                .orElseThrow(() -> new RuntimeException("Visita no encontrada"));
        return vivsoMapper.toDTO(v);
    }

    @Override
    public List<VisitaInicialDTO> FindVisitaPorTecnico(Integer tecnico) {
        List<VisitaInicial> visitas = visitaRepository.findByTecnico_Id(tecnico);
        if (visitas.isEmpty()) {
            throw new RuntimeException("No hay visitas registradas para el tecnico: " + tecnico);
        }
        return visitas.stream()
                .map(vivsoMapper::toDTO)
                .toList();
    }
}