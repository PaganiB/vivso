package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.*;
import com.vivso.Vivso.Exception.RecursoNoEncontradoException;
import com.vivso.Vivso.Exception.ReglaDeNegocioException;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.*;
import com.vivso.Vivso.Repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SolicitudService implements ISolicitudService {

    @Autowired private ISolicitudRepository solicitudRepo;
    @Autowired private IOrganizacionRepository orgRepo;
    @Autowired private IViviendaRepository viviendaRepo;
    @Autowired private IFamiliaRepository familiaRepo;
    @Autowired private IFamiliarRepository familiarRepo;
    @Autowired private IDocumentoService documentoService;
    @Autowired private VivsoMapper mapper;

    @Override
    @Transactional
    public SolicitudDTO registrarSolicitudFamilia(RegistroFamiliaDTO dto,
                                                  MultiValueMap<String, MultipartFile> mapaArchivos) {

        Organizacion org = orgRepo.findById(dto.getCuitOrg())
                .orElseThrow(() -> new RecursoNoEncontradoException("Organización no encontrada: " + dto.getCuitOrg()));

        Familia familia = familiaRepo.save(mapper.toEntity(dto.getFamilia()));

        if (dto.getFamiliares() != null) {
            for (FamiliarDTO familiarDTO : dto.getFamiliares()) {
                Familiar familiar = mapper.toEntity(familiarDTO);
                familiar.setFamilia(familia);
                familiarRepo.save(familiar);
            }
        }

        Solicitud solicitud = new Solicitud();
        solicitud.setCuitOrg(org);
        solicitud.setFamiliaBeneficiaria(familia);
        solicitud = solicitudRepo.save(solicitud);

        if (mapaArchivos != null) {
            mapaArchivos.forEach((clave, listaArchivos) -> {
                if (!clave.equals("datos") && !listaArchivos.isEmpty()) {
                    try {
                        TipoDocumento tipoDoc = TipoDocumento.valueOf(clave.toUpperCase());
                        for (MultipartFile archivo : listaArchivos) {
                            documentoService.subirDocumento(archivo, tipoDoc, familia.getIdFamilia(), null);
                        }
                    } catch (IllegalArgumentException e) {
                        log.warn("Clave de documento ignorada: {}", clave);
                    }
                }
            });
        }
        return mapper.toDTO(solicitud);
    }

    @Override
    @Transactional
    public SolicitudDTO asignarNumeroExpediente(Integer idSolicitud, String numExp) {
        Solicitud s = solicitudRepo.findById(idSolicitud)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada: " + idSolicitud));

        if (numExp == null || numExp.isBlank())
            throw new ReglaDeNegocioException("El número de expediente no puede estar vacío", HttpStatus.BAD_REQUEST);

        s.setNumExp(numExp);
        return mapper.toDTO(solicitudRepo.save(s));
    }

    @Override
    @Transactional
    public SolicitudDTO aprobarSolicitudVivienda(Integer idSolicitud) {
        Solicitud s = solicitudRepo.findById(idSolicitud)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada: " + idSolicitud));

        if (s.getEstado() == EstadoSolicitud.Aprobada)
            throw new ReglaDeNegocioException("La solicitud ya está aprobada", HttpStatus.CONFLICT);

        if (s.getNumExp() == null || s.getNumExp().isBlank())
            throw new ReglaDeNegocioException("La solicitud no tiene número de expediente asignado. Debe cargarlo antes de aprobar.", HttpStatus.CONFLICT);

        Vivienda vivienda = Vivienda.builder()
                .familia(s.getFamiliaBeneficiaria())
                .estado(EstadoVivienda.INICIADA)
                .build();
        vivienda = viviendaRepo.save(vivienda);

        s.setEstado(EstadoSolicitud.Aprobada);
        s.setVivienda(vivienda);
        s.setFechaActivacion(LocalDate.now());

        return mapper.toDTO(solicitudRepo.save(s));
    }

    @Override
    @Transactional
    public void rechazarSolicitudVivienda(Integer idSolicitud, String motivo) {
        Solicitud s = solicitudRepo.findById(idSolicitud)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada: " + idSolicitud));

        if (s.getEstado() == EstadoSolicitud.Aprobada)
            throw new ReglaDeNegocioException("No se puede rechazar una solicitud ya aprobada", HttpStatus.CONFLICT);

        s.setEstado(EstadoSolicitud.Rechazada);
        s.setObservacion(motivo);
        solicitudRepo.save(s);
    }

    @Override
    @Transactional
    public SolicitudDTO semiAprobarSolicitud(Integer idSolicitud, String motivo) {
        Solicitud s = solicitudRepo.findById(idSolicitud)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada: " + idSolicitud));

        if (s.getEstado() == EstadoSolicitud.Aprobada)
            throw new ReglaDeNegocioException("No se puede modificar una solicitud ya aprobada", HttpStatus.CONFLICT);
        if (s.getEstado() == EstadoSolicitud.Rechazada)
            throw new ReglaDeNegocioException("No se puede modificar una solicitud ya rechazada", HttpStatus.CONFLICT);

        s.setEstado(EstadoSolicitud.SemiAprobada);
        s.setObservacion(motivo);
        return mapper.toDTO(solicitudRepo.save(s));
    }

    @Override
    public List<SolicitudDTO> getSolicitudes() {
        return solicitudRepo.findAll().stream().map(mapper::toDTO).toList();
    }

    @Override
    public List<ExpedienteMovilDTO> getSolicitudesParaMovil() {
        // 1. Buscamos todas las solicitudes
        List<Solicitud> solicitudes = solicitudRepo.findAll();

        // 2. Transformamos y FILTRAMOS
        return solicitudes.stream()
                .filter(solicitud -> solicitud.getEstado() == EstadoSolicitud.Pendiente)
                .map(solicitud -> {

                    String nombreFamilia = (solicitud.getFamiliaBeneficiaria() != null)
                            ? solicitud.getFamiliaBeneficiaria().getNombreRepresentante()
                            : "Sin asignar";

                    String nombreOng = (solicitud.getCuitOrg() != null)
                            ? solicitud.getCuitOrg().getNombre()
                            : "Sin asignar";

                    return ExpedienteMovilDTO.builder()
                            .idSolicitud(solicitud.getIdSolicitud())
                            .numExp(solicitud.getNumExp())
                            .nombreRepresentanteFamilia(nombreFamilia)
                            .nombreOrganizacion(nombreOng)
                            .build();

                }).collect(Collectors.toList());
    }

    @Override
    public SolicitudDTO findSolicitud(Integer id) {
        return solicitudRepo.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada: " + id));
    }

    @Override
    public List<SolicitudDTO> buscarPorEstado(EstadoSolicitud estado) {
        List<Solicitud> result = solicitudRepo.findByEstado(estado);
        return result.stream().map(mapper::toDTO).toList();
    }

    @Override
    public List<SolicitudDTO> buscarPorOrganizacion(String cuitOrg) {
        List<Solicitud> result = solicitudRepo.findByCuitOrg_Cuit(cuitOrg);
        return result.stream().map(mapper::toDTO).toList();
    }

    @Override
    public void deleteSolicitud(Integer id) {
        Solicitud s = solicitudRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada: " + id));
        solicitudRepo.delete(s);
    }
}