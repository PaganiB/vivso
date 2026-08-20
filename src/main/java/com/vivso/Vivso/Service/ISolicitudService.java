package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.RegistroFamiliaDTO;
import com.vivso.Vivso.DTO.RegistroOrganizacionDTO;
import com.vivso.Vivso.DTO.SolicitudCompletaDTO;
import com.vivso.Vivso.DTO.SolicitudDTO;
import com.vivso.Vivso.Model.EstadoSolicitud;
import com.vivso.Vivso.Model.Solicitud;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ISolicitudService {
    List<SolicitudDTO> getSolicitudes();

    SolicitudDTO saveSolicitud(SolicitudDTO solicitudDto);

    SolicitudDTO updateSolicitud(Integer id, SolicitudDTO solicitudDto);

    void deleteSolicitud(Integer id);

    SolicitudDTO findSolicitud(Integer id);

    // Buscar por estado (Pendiente, Aprobada, Rechazada)
    List<SolicitudDTO> buscarPorEstado(EstadoSolicitud estado);

    // Buscar solicitudes asociadas a una organización específica
    List<SolicitudDTO> buscarPorOrganizacion(String cuitOrg);

    // Formulario Organizacion
    void registrarOrganizacion(RegistroOrganizacionDTO dto, MultiValueMap<String, MultipartFile> mapaArchivos);
    void aprobarOrganizacion(String cuit);
    void rechazarOrganizacion(String cuit, String motivo);
    // Formulario Familia
    SolicitudDTO registrarSolicitudFamilia(RegistroFamiliaDTO dto, MultiValueMap<String, MultipartFile> mapaArchivos);
    SolicitudDTO aprobarSolicitudVivienda(Integer idSolicitud, String numExp);
    void rechazarSolicitudVivienda(Integer idSolicitud, String motivo);
    SolicitudDTO semiAprobarSolicitud(Integer idSolicitud, String motivo);
}

