package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.VisitaObraDTO;
import com.vivso.Vivso.Exception.RecursoNoEncontradoException;
import com.vivso.Vivso.Exception.ReglaDeNegocioException;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.*;
import com.vivso.Vivso.Repository.ISolicitudRepository;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import com.vivso.Vivso.Repository.IVisitaObraRepository;
import com.vivso.Vivso.Repository.IViviendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VisitaObraService implements IVisitaObraService {

    @Autowired IVisitaObraRepository visitaObraRepo;
    @Autowired IViviendaRepository viviendaRepo;
    @Autowired IUsuarioRepository usuarioRepo;
    @Autowired ISolicitudRepository solicitudRepo;
    @Autowired VivsoMapper mapper;
    @Autowired IDocumentoService documentoService;

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

        viviendaRepo.save(vivienda);

        VisitaObra visitaGuardada = visitaObraRepo.save(visita);
        return mapper.toDTO(visitaGuardada);
    }

    @Override
    public List<VisitaObraDTO> obtenerHistorialPorVivienda(Integer idVivienda) {
        return visitaObraRepo.findByVivienda_IdViviendaOrderByFechaDesc(idVivienda)
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void registrarAvanceDeObra(Integer idSolicitud, Integer avanceFisico, String observaciones, List<MultipartFile> fotos) {

        // 1. Identificamos al técnico por el token
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario tecnico = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Técnico no encontrado"));

        // 2. TU REGLA DE NEGOCIO: Verificamos que la solicitud exista y esté Aprobada
        Solicitud solicitud = solicitudRepo.findById(idSolicitud)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada: " + idSolicitud));

        if (solicitud.getEstado() != EstadoSolicitud.Aprobada) {
            throw new ReglaDeNegocioException("No se puede registrar el avance. La solicitud debe estar en estado 'Aprobada'.", HttpStatus.CONFLICT);
        }

        // 3. Buscamos la vivienda asociada a ese expediente
        Vivienda vivienda = viviendaRepo.findBySolicitud_IdSolicitud(idSolicitud)
                .orElseThrow(() -> new ReglaDeNegocioException("El expediente aún no tiene una vivienda generada", HttpStatus.CONFLICT));

        viviendaRepo.save(vivienda);

        // 5. Registramos la visita histórica
        VisitaObra visita = new VisitaObra();
        visita.setVivienda(vivienda);
        visita.setTecnico(tecnico);
        visita.setFecha(LocalDate.now());
        visita.setAfo(avanceFisico);
        visita.setObservaciones(observaciones);

        visita = visitaObraRepo.save(visita);

        // 6. Guardamos las fotos
        if (fotos != null && !fotos.isEmpty()) {
            String cuitOrganizacion = solicitud.getCuitOrg().getCuit(); // Lo sacamos directo de la solicitud que ya buscamos arriba
            for (MultipartFile foto : fotos) {
                documentoService.subirDocumento(foto, TipoDocumento.FOTO_OBRA, vivienda.getFamilia().getIdFamilia(), cuitOrganizacion);
            }
        }
    }
}
