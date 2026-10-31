package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.VisitaHistorialDTO;
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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class VisitaInicialService implements IVisitaInicialService {

  @Autowired
  IVisitaInicialRepository visitaRepository;
  @Autowired
  ISolicitudRepository solicitudRepository;
  @Autowired
  IUsuarioRepository usuarioRepository;
  @Autowired
  VivsoMapper vivsoMapper;
  @Autowired
  CloudinaryService cloudinaryService;
  @Autowired
  IUsuarioRepository usuarioRepo;

  @Override
  public List<VisitaInicialDTO> getVisitas() {
    return visitaRepository.findAll().stream()
      .map(vivsoMapper::toDTO)
      .toList();
  }

  //Metodo para guardar la visita CON FOTOS
  @Override
  public VisitaInicialDTO saveVisita(VisitaInicialDTO dto, List<MultipartFile> fotos) {
    String emailAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();
    Usuario tecnico = usuarioRepository.findByEmail(emailAutenticado)
      .orElseThrow(() -> new RecursoNoEncontradoException("Técnico no encontrado"));

    Solicitud solicitud = solicitudRepository.findById(dto.getIdSolicitud())
      .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada"));

    // CORRECCIÓN IDOR: Verificar que el expediente esté asignado a ESTE técnico.
    if (solicitud.getTecnicoAsignado() == null || !solicitud.getTecnicoAsignado().getId().equals(tecnico.getId())) {
      throw new ReglaDeNegocioException("No puedes registrar una visita sobre un expediente que no tienes asignado.", HttpStatus.FORBIDDEN);
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

    String emailAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();

    // Solo el dueño (o Admin) puede editar
    if (!v.getTecnico().getEmail().equals(emailAutenticado)) {
      throw new ReglaDeNegocioException("No puedes modificar la visita de otro técnico.", HttpStatus.FORBIDDEN);
    }

    vivsoMapper.updateFromDto(dto, v);
    //Forzamos mantener el técnico original, ignorando lo que envíe el cliente
    v.setTecnico(v.getTecnico());

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

  @Override
  public List<VisitaHistorialDTO> obtenerHistorialGeneral() {
    // 1. Identificamos al técnico autenticado mediante el Token JWT
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    Usuario tecnico = usuarioRepo.findByEmail(email)
      .orElseThrow(() -> new RecursoNoEncontradoException("Técnico no encontrado"));

    List<VisitaHistorialDTO> historialUnificado = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // 2. Buscamos y mapeamos las Visitas Iniciales del técnico
    List<VisitaInicial> iniciales = visitaRepository.findByTecnico_Id(tecnico.getId());

    List<VisitaHistorialDTO> dtoIniciales = iniciales.stream().map(v ->
      VisitaHistorialDTO.builder()
        .id("inicial_" + v.getId())
        .tipo("INICIAL")
        .expediente(v.getSolicitud() != null ? v.getSolicitud().getNumExp() : "S/N")
        .localidad(v.getLocalidad() != null ? v.getLocalidad() : "Sin localidad")
        .departamento(v.getDepartamento() != null ? v.getDepartamento() : "Sin depto")
        .fecha(v.getFechaVisita() != null ? v.getFechaVisita().format(formatter) : "")
        .clasificacion(v.getClasificacionPreliminar() != null ? v.getClasificacionPreliminar().getValor() : "-")
        .estado("sincronizada")
        .estadoSolicitud(v.getSolicitud() != null && v.getSolicitud().getEstado() != null ? v.getSolicitud().getEstado().name() : "Pendiente")
        .avanceActual(0)
        .cantHabitantesReal(v.getCantHabitantesReal())
        .tienePrioridadSocial(v.getTienePrioridadSocial())
        .observaciones(v.getObservaciones())
        .lat(v.getLat())
        .lng(v.getLng())
        .urlsFotos(v.getUrlsFotos())
        .tecnicoEmail(v.getTecnico() != null ? v.getTecnico().getEmail() : "Sin técnico")
        .build()
    ).toList();

    historialUnificado.addAll(dtoIniciales);

    return historialUnificado;
  }

}