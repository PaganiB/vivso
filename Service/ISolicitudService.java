package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.*;
import com.vivso.Vivso.Model.EstadoSolicitud;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ISolicitudService {
    List<SolicitudDTO> getSolicitudes();
    void deleteSolicitud(Integer id);

    SolicitudDTO findSolicitud(Integer id);

    //Buscar por tecnico
    List<ExpedienteWebDTO> obtenerDashboardWebTecnico();
    List<ExpedienteMovilDTO> getSolicitudesParaMovil();

    // Buscar por estado (Pendiente, Aprobada, Rechazada)
    List<SolicitudDTO> buscarPorEstado(EstadoSolicitud estado);

    // Buscar solicitudes asociadas a una organización específica
    List<SolicitudDTO> buscarPorOrganizacion(String cuitOrg);

    SolicitudDTO asignarNumeroExpediente(Integer idSolicitud, String numExp);

    //Enviar obras al Integrante de la ONG en la app movil
    List<ExpedienteMovilDTO> getObrasAsignadasParaOng();

    // Formulario Familia
    SolicitudDTO registrarSolicitudFamilia(RegistroFamiliaDTO dto, MultiValueMap<String, MultipartFile> mapaArchivos);
    SolicitudDTO aprobarSolicitudVivienda(Integer idSolicitud);
    void rechazarSolicitudVivienda(Integer idSolicitud, String motivo);
    SolicitudDTO semiAprobarSolicitud(Integer idSolicitud, List<String> camposObservados, String motivo);
    void confirmarCorreccion(Integer idSolicitud);
}