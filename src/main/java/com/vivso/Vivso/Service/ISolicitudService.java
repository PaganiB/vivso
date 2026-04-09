package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.SolicitudDTO;
import com.vivso.Vivso.Model.EstadoSolicitud;
import com.vivso.Vivso.Model.Solicitud;

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
}

