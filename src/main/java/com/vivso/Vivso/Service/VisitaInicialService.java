package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.VisitaInicialDTO;
import com.vivso.Vivso.Exception.RecursoNoEncontradoException;
import com.vivso.Vivso.Exception.ReglaDeNegocioException;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.Solicitud;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Model.VisitaInicial;
import com.vivso.Vivso.Repository.ISolicitudRepository;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import com.vivso.Vivso.Repository.IVisitaInicialRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;

@Service
public class VisitaInicialService implements IVisitaInicialService {

    @Autowired IVisitaInicialRepository visitaRepository;
    @Autowired ISolicitudRepository solicitudRepository;
    @Autowired IUsuarioRepository usuarioRepository;
    @Autowired VivsoMapper vivsoMapper;
    @Autowired CloudinaryService cloudinaryService;


    @Override
    public List<VisitaInicialDTO> getVisitas() {
        return visitaRepository.findAll().stream()
                .map(vivsoMapper::toDTO)
                .toList();
    }

    //Metodo para guardar la visita CON FOTOS
    @Override
    @Transactional // Recomendado al guardar entidad + procesar archivos
    public VisitaInicialDTO saveVisita(VisitaInicialDTO dto, List<MultipartFile> fotos) {

        String emailAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario tecnico = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Técnico no encontrado con email: " + emailAutenticado));

        Solicitud solicitud = solicitudRepository.findById(dto.getIdSolicitud())
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud de vivienda no encontrada con ID: " + dto.getIdSolicitud()));

      if (visitaRepository.existsBySolicitud_IdSolicitud(solicitud.getIdSolicitud())) {
        throw new ReglaDeNegocioException(
          "Esta solicitud ya tiene una visita inicial registrada", HttpStatus.CONFLICT);
      }

        VisitaInicial v = vivsoMapper.toEntity(dto);
        v.setTecnico(tecnico);
        v.setSolicitud(solicitud);

        List<String> rutas = new ArrayList<>();

        if (fotos != null && !fotos.isEmpty()) {
            for (MultipartFile foto : fotos) {
                if (!foto.isEmpty()) {
                  String ruta = cloudinaryService.subirArchivo(foto, "visitaInicial");
                    rutas.add(ruta);
                }
            }
        }

        v.setUrlsFotos(rutas);
        return vivsoMapper.toDTO(visitaRepository.save(v));
    }

    @Override
    @Transactional
    public VisitaInicialDTO updateVisita(Integer idVisita, VisitaInicialDTO dto) {
        VisitaInicial v = visitaRepository.findById(idVisita)
                .orElseThrow(() -> new RecursoNoEncontradoException("Visita no encontrada"));

        vivsoMapper.updateFromDto(dto, v);

        v.setTecnico(usuarioRepository.findById(dto.getIdTecnico())
                .orElseThrow(() -> new RecursoNoEncontradoException("Tecnico no encontrado")));

        return vivsoMapper.toDTO(visitaRepository.save(v));
    }

    @Override
    public void deleteVisita(Integer idVisita) {
        VisitaInicial v = visitaRepository.findById(idVisita)
                .orElseThrow(() -> new RecursoNoEncontradoException("Visita no encontrada"));
        visitaRepository.delete(v);
    }

    @Override
    public VisitaInicialDTO FindVisita(Integer idVisita) {
        VisitaInicial v = visitaRepository.findById(idVisita)
                .orElseThrow(() -> new RecursoNoEncontradoException("Visita no encontrada"));
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