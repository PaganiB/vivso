package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.SolicitudDTO;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.*;
import com.vivso.Vivso.Repository.IFamiliaRepository;
import com.vivso.Vivso.Repository.IOrganizacionRepository;
import com.vivso.Vivso.Repository.ISolicitudRepository;
import com.vivso.Vivso.Repository.IViviendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SolicitudService implements ISolicitudService {

    @Autowired private ISolicitudRepository solicitudRepo;
    @Autowired private IOrganizacionRepository orgRepo;
    @Autowired private IViviendaRepository viviendaRepo;
    @Autowired private IFamiliaRepository familiaRepo;
    @Autowired private VivsoMapper mapper;

    @Override
    public List<SolicitudDTO> getSolicitudes() {
        return solicitudRepo.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public SolicitudDTO saveSolicitud(SolicitudDTO dto) {
        Organizacion org = orgRepo.findById(dto.getCuitOrg())
                .orElseThrow(() -> new RuntimeException("Organización no encontrada: " + dto.getCuitOrg()));

        Vivienda viv = null;
        if (dto.getEstado() == EstadoSolicitud.Aprobada) {
            if (dto.getNumExp() == null || dto.getNumExp().isBlank())
                throw new RuntimeException("Debe indicar el número de expediente para solicitudes aprobadas");
            viv = viviendaRepo.findViviendaByNumExp(dto.getNumExp())
                    .orElseThrow(() -> new RuntimeException("Vivienda no encontrada: " + dto.getNumExp()));
        }

        Familia familia = familiaRepo.findById(dto.getIdFamilia())
                .orElseThrow(() -> new RuntimeException("Familia no encontrada: " + dto.getIdFamilia()));

        Solicitud s = mapper.toEntity(dto);
        s.setCuitOrg(org);
        s.setNumExp(viv);
        s.setFamiliaBeneficiaria(familia);
        s.setFechaActivacion(dto.getEstado() == EstadoSolicitud.Aprobada ? LocalDate.now() : null);

        return mapper.toDTO(solicitudRepo.save(s));
    }

    @Override
    public SolicitudDTO updateSolicitud(Integer id, SolicitudDTO dto) {
        Solicitud s = solicitudRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + id));

        if (dto.getGDE() != null)            s.setGDE(dto.getGDE());
        if (dto.getFechaSolicitud() != null) s.setFechaSolicitud(dto.getFechaSolicitud());
        if (dto.getObservacion() != null)    s.setObservacion(dto.getObservacion());

        if (dto.getCuitOrg() != null) {
            s.setCuitOrg(orgRepo.findById(dto.getCuitOrg())
                    .orElseThrow(() -> new RuntimeException("Organización no encontrada: " + dto.getCuitOrg())));
        }

        if (dto.getIdFamilia() != null) {
            s.setFamiliaBeneficiaria(familiaRepo.findById(dto.getIdFamilia())
                    .orElseThrow(() -> new RuntimeException("Familia no encontrada: " + dto.getIdFamilia())));
        }

        if (dto.getEstado() != null) s.setEstado(dto.getEstado());

        // Lógica de estado: numExp y fechaActivacion van juntos
        EstadoSolicitud estadoFinal = s.getEstado();
        if (estadoFinal == EstadoSolicitud.Aprobada) {
            if (dto.getNumExp() == null || dto.getNumExp().isBlank())
                throw new RuntimeException("Debe indicar el número de expediente para solicitudes aprobadas");
            s.setNumExp(viviendaRepo.findViviendaByNumExp(dto.getNumExp())
                    .orElseThrow(() -> new RuntimeException("Expediente no encontrado: " + dto.getNumExp())));
            if (s.getFechaActivacion() == null) s.setFechaActivacion(LocalDate.now());
        } else {
            s.setNumExp(null);
            s.setFechaActivacion(null);
        }

        return mapper.toDTO(solicitudRepo.save(s));
    }

    @Override
    public void deleteSolicitud(Integer id) {
        Solicitud s = solicitudRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + id));
        solicitudRepo.delete(s);
    }

    @Override
    public SolicitudDTO findSolicitud(Integer id) {
        return solicitudRepo.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + id));
    }

    @Override
    public List<SolicitudDTO> buscarPorEstado(EstadoSolicitud estado) {
        List<Solicitud> result = solicitudRepo.findByEstado(estado);
        if (result.isEmpty()) throw new RuntimeException("Ninguna solicitud tiene ese estado");
        return result.stream().map(mapper::toDTO).toList();
    }

    @Override
    public List<SolicitudDTO> buscarPorOrganizacion(String cuitOrg) {
        List<Solicitud> result = solicitudRepo.findByCuitOrg_Cuit(cuitOrg);
        if (result.isEmpty()) throw new RuntimeException("No hay solicitudes para el CUIT: " + cuitOrg);
        return result.stream().map(mapper::toDTO).toList();
    }
}
