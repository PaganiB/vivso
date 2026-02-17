package com.vivso.Vivso.Service;

import com.vivso.Vivso.Model.EstadoSolicitud;
import com.vivso.Vivso.Model.Solicitud;

import java.util.List;
import java.util.Optional;

public interface ISolicitudService {
    List<Solicitud> listarTodas();
    Solicitud buscarPorId(Integer id);
    Solicitud guardar(Solicitud solicitud);
    void eliminar(Integer id);

    // Buscar por estado (Pendiente, Aprobada, Rechazada, Adjudicada)
    List<Solicitud> buscarPorEstado(EstadoSolicitud estado);

    // Buscar solicitudes asociadas a una organización específica
    List<Solicitud> buscarPorOrganizacion(String cuitOrg);
}

