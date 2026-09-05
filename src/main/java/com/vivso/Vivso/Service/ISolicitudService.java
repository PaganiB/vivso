package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.*;
import com.vivso.Vivso.Model.EstadoSolicitud;
import com.vivso.Vivso.Model.Solicitud;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ISolicitudService {
    List<SolicitudDTO> getSolicitudes();

    void deleteSolicitud(Integer id);

    SolicitudDTO findSolicitud(Integer id);

    // Buscar por estado (Pendiente, Aprobada, Rechazada)
    List<SolicitudDTO> buscarPorEstado(EstadoSolicitud estado);

    // Buscar solicitudes asociadas a una organización específica
    List<SolicitudDTO> buscarPorOrganizacion(String cuitOrg);

    SolicitudDTO asignarNumeroExpediente(Integer idSolicitud, String numExp);

    // Formulario Organizacion
    void registrarOrganizacion(RegistroOrganizacionDTO dto, MultiValueMap<String, MultipartFile> mapaArchivos);
    void aprobarOrganizacion(String cuit);
    void rechazarOrganizacion(String cuit, String motivo);
    void marcarObservaciones(Integer idSolicitud, List<String> camposObservados, String motivo);
    SolicitudOrganizacionDTO obtenerSolicitudOrgPorToken(String token);
    void actualizarSolicitudOrgConToken(String token, SolicitudOrganizacionPatchDTO dto);
    // Formulario Familia
    SolicitudDTO registrarSolicitudFamilia(RegistroFamiliaDTO dto, MultiValueMap<String, MultipartFile> mapaArchivos);
    SolicitudDTO aprobarSolicitudVivienda(Integer idSolicitud);
    void rechazarSolicitudVivienda(Integer idSolicitud, String motivo);
    SolicitudDTO semiAprobarSolicitud(Integer idSolicitud, String motivo);
}

