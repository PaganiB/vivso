package com.vivso.Vivso.Service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    //Metodo para guardar la visita CON FOTOS
    @Override
    @Transactional // Recomendado al guardar entidad + procesar archivos
    public VisitaInicialDTO saveVisita(VisitaInicialDTO dto, List<MultipartFile> fotos) {

        String emailAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Buscamos al técnico por su email real, sin depender de lo que envíe el celular
        Usuario tecnico = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Técnico no encontrado con email: " + emailAutenticado));

        // 3. Buscamos la solicitud (Esto queda igual que antes)
        Solicitud solicitud = solicitudRepository.findById(dto.getIdSolicitud())
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud de vivienda no encontrada con ID: " + dto.getIdSolicitud()));

        // 4. Mapeamos el DTO a la entidad
        VisitaInicial v = vivsoMapper.toEntity(dto);
        v.setTecnico(tecnico);
        v.setSolicitud(solicitud);

        // 3. Procesamos las fotos si es que enviaron alguna
        List<String> rutas = new ArrayList<>();

        if (fotos != null && !fotos.isEmpty()) {
            for (MultipartFile foto : fotos) {
                if (!foto.isEmpty()) {
                    // Usamos el método auxiliar para guardar en disco
                    String ruta = guardarArchivoFisico(foto);
                    rutas.add(ruta);
                }
            }
        }

        // Asignamos la lista de rutas generadas a la entidad
        v.setUrlsFotos(rutas);

        // 4. Guardamos en MariaDB y devolvemos el DTO
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
            // Generamos un nombre único con UUID para que no haya conflictos
            String nombreArchivo = UUID.randomUUID().toString() + "_" + archivo.getOriginalFilename();

            // Definimos la carpeta donde se guardarán (ej: uploads/visitas)
            Path rutaDirectorio = Paths.get("uploads/visitas");

            // Si la carpeta no existe, la crea automáticamente
            if (!Files.exists(rutaDirectorio)) {
                Files.createDirectories(rutaDirectorio);
            }

            // Guardamos el archivo en el disco
            Path rutaArchivo = rutaDirectorio.resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

            return rutaArchivo.toString();

        } catch (IOException e) {
            // Usamos tu excepción genérica para que el controlador ataje el error 500/409
            throw new RuntimeException("Error al guardar la foto en el servidor: " + e.getMessage());
        }
    }
}