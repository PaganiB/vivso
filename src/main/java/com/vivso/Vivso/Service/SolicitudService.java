package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.FamiliaDTO;
import com.vivso.Vivso.DTO.OrganizacionDTO;
import com.vivso.Vivso.DTO.SolicitudDTO;
import com.vivso.Vivso.Mapper.Mapper;
import com.vivso.Vivso.Model.*;
import com.vivso.Vivso.Repository.IOrganizacionRepository;
import com.vivso.Vivso.Repository.ISolicitudRepository;
import com.vivso.Vivso.Repository.IViviendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.cfg.MapperBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
public class SolicitudService implements ISolicitudService {

    @Autowired
    private ISolicitudRepository solicitudRepo;
    @Autowired
    private IOrganizacionRepository orgRepo;
    @Autowired
    private IViviendaRepository viviendaRepo;
    @Autowired
    private IOrganizacionRepository Organizacion;

    @Override
    public List<SolicitudDTO> getSolicitudes() {
        return solicitudRepo.findAll().stream()
                .map(Mapper::toDTO)
                .toList();
    }

    @Override
    public SolicitudDTO saveSolicitud(SolicitudDTO solicitudDto) {

        // 1. Buscamos la organización
        Organizacion org = orgRepo.findById(solicitudDto.getCuitOrg())
                .orElseThrow(() -> new RuntimeException("Organización no encontrada con el CUIT: " + solicitudDto.getCuitOrg()));

        Vivienda viv = null;

        // 2. Validación condicional según estado
        if (solicitudDto.getEstado() == EstadoSolicitud.Aprobada) {

            if (solicitudDto.getNumExp() == null || solicitudDto.getNumExp().isBlank()) {
                throw new RuntimeException("Debe indicar el número de expediente para solicitudes aprobadas");
            }

            viv = viviendaRepo.findViviendaByNumExp(solicitudDto.getNumExp())
                    .orElseThrow(() -> new RuntimeException("Vivienda no encontrada"));
        }

        // 3. Construcción de la entidad
        var s = Solicitud.builder()
                .id_solicitud(solicitudDto.getIdSolicitud())
                .GDE(solicitudDto.getGDE())
                .fechaSolicitud(solicitudDto.getFechaSolicitud())
                .cuitOrg(org)
                .numExp(viv) // puede ser null si no está aprobada
                .estado(solicitudDto.getEstado())
                .observacion(solicitudDto.getObservacion())
                .build();

        return Mapper.toDTO(solicitudRepo.save(s));
    }

    @Override
    public SolicitudDTO updateSolicitud(Integer id, SolicitudDTO solicitudDto) {

        // 1. Buscamos la solicitud existente
        Solicitud s = solicitudRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("No se puede editar: solicitud no encontrada con ID " + id));

        // 2. Actualizamos campos simples
        if (solicitudDto.getGDE() != null) {
            s.setGDE(solicitudDto.getGDE());
        }

        if (solicitudDto.getFechaSolicitud() != null) {
            s.setFechaSolicitud(solicitudDto.getFechaSolicitud());
        }

        if (solicitudDto.getObservacion() != null) {
            s.setObservacion(solicitudDto.getObservacion());
        }

        // 3. Organización (obligatoria pero puede actualizarse)
        if (solicitudDto.getCuitOrg() != null) {
            Organizacion org = orgRepo.findById(solicitudDto.getCuitOrg())
                    .orElseThrow(() -> new RuntimeException("Organización no encontrada"));
            s.setCuitOrg(org);
        }

        // 4. Estado (importante para lógica de negocio)
        if (solicitudDto.getEstado() != null) {
            s.setEstado(solicitudDto.getEstado());
        }

        // 5. Manejo de numExp según estado FINAL
        EstadoSolicitud estadoFinal = s.getEstado();

        if (estadoFinal == EstadoSolicitud.Aprobada) {

            if (solicitudDto.getNumExp() == null || solicitudDto.getNumExp().isBlank()) {
                throw new RuntimeException("Debe indicar el número de expediente para solicitudes aprobadas");
            }

            Vivienda viv = viviendaRepo.findViviendaByNumExp(solicitudDto.getNumExp())
                    .orElseThrow(() -> new RuntimeException("Número de expediente no encontrado"));

            s.setNumExp(viv);

        } else {
            // Si no está aprobada → no debe tener expediente
            s.setNumExp(null);
        }

        // 6. Guardamos y devolvemos
        return Mapper.toDTO(solicitudRepo.save(s));
    }

    @Override
    public void deleteSolicitud(Integer id) {
        // Buscamos la solicitud existente o lanzamos error si no está
        Solicitud s = solicitudRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        solicitudRepo.delete(s);
    }

    @Override
    public SolicitudDTO findSolicitud(Integer id) {
        return solicitudRepo.findById(id)
                .map(Mapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con el ID: " + id));
    }

    @Override
    public List<SolicitudDTO> buscarPorEstado(EstadoSolicitud estado) {
        //Buscamos las solicitudes con el estado correspondiente
        List<Solicitud> s = solicitudRepo.findByEstado(estado);
        if (s.isEmpty()) {
            throw new RuntimeException("Ninguna solicitud tiene ese estado");
        }
        //Creamos una lista para guardar los dto
        List<SolicitudDTO> dto = new ArrayList<>();
        for (Solicitud solicitud : s) {
            dto.add(Mapper.toDTO(solicitud));
        }
        return dto;
    }

    @Override
    public List<SolicitudDTO> buscarPorOrganizacion(String cuitOrg) {
        List<Solicitud> solicitudes = solicitudRepo.findByCuitOrg_Cuit(cuitOrg);

        if (solicitudes.isEmpty()) {
            throw new RuntimeException("No hay solicitudes para la organización con CUIT: " + cuitOrg);
        }

        return solicitudes.stream()
                .map(Mapper::toDTO)
                .toList();
    }
}
