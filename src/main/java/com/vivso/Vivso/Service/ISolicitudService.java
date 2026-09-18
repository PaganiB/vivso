package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.*;
import com.vivso.Vivso.Model.EstadoSolicitud;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ISolicitudService {
    List<SolicitudDTO> getSolicitudes();
    List<ExpedienteMovilDTO> getSolicitudesParaMovil();
    void deleteSolicitud(Integer id);

    SolicitudDTO findSolicitud(Integer id);

    // Buscar por estado (Pendiente, Aprobada, Rechazada)
    List<SolicitudDTO> buscarPorEstado(EstadoSolicitud estado);

    // Buscar solicitudes asociadas a una organización específica
    List<SolicitudDTO> buscarPorOrganizacion(String cuitOrg);

    SolicitudDTO asignarNumeroExpediente(Integer idSolicitud, String numExp);

    // Formulario Familia
    SolicitudDTO registrarSolicitudFamilia(RegistroFamiliaDTO dto, MultiValueMap<String, MultipartFile> mapaArchivos);
    SolicitudDTO aprobarSolicitudVivienda(Integer idSolicitud);
    void rechazarSolicitudVivienda(Integer idSolicitud, String motivo);
    SolicitudDTO semiAprobarSolicitud(Integer idSolicitud, String motivo);
}