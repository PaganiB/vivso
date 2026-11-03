package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.*;
import com.vivso.Vivso.Exception.RecursoNoEncontradoException;
import com.vivso.Vivso.Exception.ReglaDeNegocioException;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.*;
import com.vivso.Vivso.Repository.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizacionService implements IOrganizacionService {

    @Autowired private IOrganizacionRepository orgRepo;
    @Autowired private VivsoMapper mapper;
    @Autowired private IIntegranteRepository integranteRepo;
    @Autowired private ISolicitudRepository solicitudRepo;
    @Autowired private IFamiliarRepository familiarRepo;
    @Autowired private IViviendaRepository viviendaRepo;
    @Autowired private IEtapaObraRepository etapaObraRepo;

    @Override
    public List<OrganizacionDTO> getOrganizacion() {
        return orgRepo.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public OrganizacionDTO updateOrganizacion(String cuit, OrganizacionDTO dto) {
        Organizacion o = orgRepo.findById(cuit)
                .orElseThrow(() -> new RecursoNoEncontradoException("Organización no encontrada: " + cuit));

        // Aplica solo los campos no nulos — sin if por campo, el CUIT (PK) está ignorado en el mapper
        mapper.updateFromDto(dto, o);

        return mapper.toDTO(orgRepo.save(o));
    }

    @Override
    public void deleteOrganizacion(String cuit) {
        Organizacion o = orgRepo.findById(cuit)
                .orElseThrow(() -> new RecursoNoEncontradoException("Organización no encontrada: " + cuit));
        orgRepo.delete(o);
    }

    @Override
    public OrganizacionDTO buscarPorCuit(String cuit) {
        return orgRepo.findById(cuit)
                .map(mapper::toDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Organización no encontrada: " + cuit));
    }

    // buscarPorTipo
    @Override
    public List<OrganizacionDTO> buscarPorTipo(String tipo) {
        List<Organizacion> result = orgRepo.findOrganizacionByTipo(tipo);
        return result.stream().map(mapper::toDTO).toList();
    }

    // buscarPorNombre
    @Override
    public List<OrganizacionDTO> buscarPorNombre(String nombre) {
        List<Organizacion> result = orgRepo.findByNombreContainingIgnoreCase(nombre);
        return result.stream().map(mapper::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizacionDesarrolloSocialDTO> obtenerVistaDesarrolloSocial() {

        return orgRepo.findAll().stream()
                .map(organizacion -> {

                    // 1. Obtener todos los integrantes de la organización
                    List<IntegranteDTO> integrantes =
                            integranteRepo.findByOrganizacion_CuitAndActivoTrue(organizacion.getCuit())
                                    .stream()
                                    .map(mapper::toDTO)
                                    .toList();

                    // 2. Obtener todas las solicitudes de la organización
                    List<ProyectoOrganizacionDTO> proyectos =
                            solicitudRepo.findByCuitOrg_Cuit(organizacion.getCuit())
                                    .stream()
                                    .map(solicitud -> {

                                        // Familia beneficiaria
                                        Familia familia = solicitud.getFamiliaBeneficiaria();

                                        FamiliaDTO familiaDTO = familia != null
                                                ? mapper.toDTO(familia)
                                                : null;

                                        // Familiares
                                        List<FamiliarDTO> familiares = familia != null
                                                ? familiarRepo.findByFamilia_IdFamilia(familia.getIdFamilia())
                                                .stream()
                                                .map(mapper::toDTO)
                                                .toList()
                                                : List.of();

                                        // Vivienda
                                        ViviendaDTO viviendaDTO =
                                                viviendaRepo.findBySolicitud_IdSolicitud(solicitud.getIdSolicitud())
                                                        .map(mapper::toDTO)
                                                        .orElse(null);

                                        // Etapas de obra
                                        List<EtapaObra> etapas =
                                                etapaObraRepo.findBySolicitud_IdSolicitud(solicitud.getIdSolicitud());

                                        List<EtapaObraDTO> etapasDTO = etapas.stream()
                                                .map(mapper::toDTO)
                                                .toList();

                                        // Estado general de la obra
                                        String estadoGeneral = calcularEstadoGeneralObra(etapas);

                                        return ProyectoOrganizacionDTO.builder()
                                                .idSolicitud(solicitud.getIdSolicitud())
                                                .numeroExpediente(solicitud.getNumExp())
                                                .fechaSolicitud(solicitud.getFechaSolicitud())
                                                .estadoSolicitud(solicitud.getEstado())
                                                .familia(familiaDTO)
                                                .familiares(familiares)
                                                .vivienda(viviendaDTO)
                                                .etapasObra(etapasDTO)
                                                .estadoGeneralObra(estadoGeneral)
                                                .build();
                                    })
                                    .toList();

                    // 3. Construir la organización con sus integrantes y proyectos
                    return OrganizacionDesarrolloSocialDTO.builder()
                            .cuit(organizacion.getCuit())
                            .nombre(organizacion.getNombre())
                            .tipo(organizacion.getTipo())
                            .dom_legal(organizacion.getDom_legal())
                            .cpe(organizacion.getCpe())
                            .integrantes(integrantes)
                            .proyectos(proyectos)
                            .build();
                })
                .toList();
    }

    private String calcularEstadoGeneralObra(List<EtapaObra> etapas) {

        if (etapas.isEmpty()) {
            return "SIN_INICIAR";
        }

        boolean todasAprobadas =
                etapas.size() == EtapaConstruccion.values().length
                        && etapas.stream()
                        .allMatch(etapa -> etapa.getEstado() == EstadoEtapa.APROBADA);

        if (todasAprobadas) {
            return "FINALIZADA";
        }

        return "EN_CONSTRUCCION";
    }

}
