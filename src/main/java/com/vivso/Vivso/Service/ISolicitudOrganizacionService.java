package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ISolicitudOrganizacionService {
    void registrarOrganizacion(RegistroOrganizacionDTO dto, MultiValueMap<String, MultipartFile> mapaArchivos);
    void aprobarOrganizacion(String cuit);
    void rechazarOrganizacion(String cuit, String motivo);
    void marcarObservaciones(Integer idSolicitud, List<String> camposObservados, String motivo);
    SolicitudOrganizacionDTO obtenerSolicitudOrgPorToken(String token);
    void actualizarSolicitudOrgConToken(String token, SolicitudOrganizacionPatchDTO dto);
    void reemplazarDocumentoConToken(String token, Integer idDoc, MultipartFile archivo);
}