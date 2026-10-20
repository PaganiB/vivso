package com.vivso.Vivso.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.vivso.Vivso.DTO.VisitaInicialDTO;
import com.vivso.Vivso.Exception.RecursoNoEncontradoException;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.Solicitud;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Model.VisitaInicial;
import com.vivso.Vivso.Repository.ISolicitudRepository;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import com.vivso.Vivso.Repository.IVisitaInicialRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class VisitaInicialService implements IVisitaInicialService {

    @Autowired IVisitaInicialRepository visitaRepository;
    @Autowired ISolicitudRepository solicitudRepository;
    @Autowired IUsuarioRepository usuarioRepository;
    @Autowired VivsoMapper vivsoMapper;
    @Autowired
    public VisitaInicialService(@Value("${CLOUDINARY_URL}") String cloudinaryUrl) {
        this.cloudinary = new Cloudinary(cloudinaryUrl);
    }
    private final Cloudinary cloudinary;

    @Value("${vivso.upload.dir}/visitas")
    private String carpetaVisitas;

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

        VisitaInicial v = vivsoMapper.toEntity(dto);
        v.setTecnico(tecnico);
        v.setSolicitud(solicitud);

        List<String> rutas = new ArrayList<>();

        if (fotos != null && !fotos.isEmpty()) {
            for (MultipartFile foto : fotos) {
                if (!foto.isEmpty()) {
                    String ruta = guardarArchivoFisico(foto);
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

    //METODO AUXILAR
    private String guardarArchivoFisico(MultipartFile archivo) {
        try {
            Map resultado = cloudinary.uploader().upload(archivo.getBytes(),
                    ObjectUtils.asMap("folder", "vivso_visitas")
            );
            return resultado.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Error al subir la foto: " + e.getMessage());
        }
    }
}