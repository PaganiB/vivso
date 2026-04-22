package com.vivso.Vivso.Mapper;

import com.vivso.Vivso.DTO.*;
import com.vivso.Vivso.Model.*;

public class Mapper {

    //Mapeo de Familia a DTO
    public static FamiliaDTO toDTO(Familia f ) {
        if (f == null) return null;

        return FamiliaDTO.builder()
                .idFamilia(f.getId_familia())
                .nombreRepresentante(f.getNombreRepresentante())
                .contacto(f.getContacto())
                .antiguedadRancho(f.getAntiguedadRancho())
                .coordenadasRancho(f.getCoordenadasRancho())
                .cuitOrg(f.getCuitOrg().getCuit())
                .build();
    }
    //Mapeo de Familiar a DTO
    public static FamiliarDTO toDTO(Familiar iF){
        if (iF == null) return null;

        return FamiliarDTO.builder()
                .idIntegranteFamilia(iF.getIdIntegranteFamilia())
                .dni(iF.getDni())
                .nombre(iF.getNombre())
                .apellido(iF.getApellido())
                .condicion_especial(iF.getCondicion_especial() != null ?
                        iF.getCondicion_especial() : "No posee discapacidad")
                .familia(iF.getFamilia().getId_familia())
                .usuario(iF.getUsuario() != null ? iF.getUsuario().getUsername() : "Sin usuario")
                .build();
    }

    //Mapeo de Organizacion a DTO
    public static OrganizacionDTO toDTO(Organizacion o){
        if (o == null) return null;

        return OrganizacionDTO.builder()
                .cuit(o.getCuit())
                .nombre(o.getNombre())
                .tipo(o.getTipo())
                .dom_legal(o.getDom_legal())
                .contacto(o.getContacto())
                .cpe(o.getCpe())
                .build();
    }

    //Mapeo de Integrante a DTO
    public static IntegranteDTO toDTO(Integrante iO){
        if (iO == null) return null;

        return IntegranteDTO.builder()
                .idIntegrante(iO.getId_integranteOrganizacion())
                .dni(iO.getDni())
                .nombre(iO.getNombre())
                .apellido(iO.getApellido())
                .telefono(iO.getTelefono())
                .funcion(iO.getFuncion())
                .domicilio(iO.getDomicilio())
                .activo(iO.getActivo())
                .cuitOrg(iO.getOrganizacion().getCuit())
                .usuario(iO.getUsuario() != null ? iO.getUsuario().getUsername() : "Sin usuario")
                .build();
    }

    //Mapeo de Vivienda a DTO
    public static ViviendaDTO toDTO(Vivienda v){
        if (v == null) return null;

        return com.vivso.Vivso.DTO.ViviendaDTO.builder()
                .numExp(v.getNumExp()).
                departamento(v.getDepartamento()).
                localidad(v.getLocalidad()).
                direccion(v.getDireccion()).
                superficie(v.getSuperficie()).
                fechaInic(v.getFechaInic()).
                fechaFin(v.getFechaFin()).
                estado(v.getEstado()).
                observacion(v.getObservacion()).
                id_familia(v.getFamilia() != null ? v.getFamilia().getId_familia() : null).
                representante(v.getFamilia() != null ? v.getFamilia().getNombreRepresentante() : "Sin asignar").
                build();
    }

    // Mapeo de Solicitud a DTO
    public static SolicitudDTO toDTO(Solicitud s){
        if (s == null) return null;

        return SolicitudDTO.builder()
                .idSolicitud(s.getId_solicitud())
                .GDE(s.getGDE())
                .fechaSolicitud(s.getFechaSolicitud())
                .cuitOrg(s.getCuitOrg().getCuit())
                .numExp(
                        s.getNumExp() != null
                                ? s.getNumExp().getNumExp()
                                : null
                )
                .estado(s.getEstado())
                .observacion(s.getObservacion())
                .build();
    }

    //Mapeo de Documento a DTO
    public static DocumentoDTO toDTO(Documento d){
        if (d == null) return null;

        return DocumentoDTO.builder()
                .idDocumento(d.getId())
                .nombre(d.getNombreOriginal())
                .url(d.getUrlPath())
                .tipo(d.getTipo())
                .estado(d.getEstado())
                .motivoRechazo(d.getMotivoRechazo())
                .fechaSubida(d.getFechaSubida())
                .fechaRevision(d.getFechaRevision())
                .id_familia(d.getFamilia() != null ? d.getFamilia().getId_familia() : null)
                .cuitOrg(d.getOrganizacion() != null ? d.getOrganizacion().getCuit() : null)
                .idUsuarioRevisor(d.getRevisor() !=null ? d.getRevisor().getId() : null)
                .nombreRevisor(d.getRevisor() != null ? d.getRevisor().getUsername() : "Sin asignar")
                .build();
    }

    // Mapeo de Usuario a RespuestaDTO
    public static UsuarioRespuestaDTO toRespuestaDTO(Usuario u) {
        if (u == null) return null;

        return UsuarioRespuestaDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .rol(u.getRol() != null ? u.getRol() : "SIN_ROL")
                .build();
    }

    //Mapeo de Usuario a RegistroDTO
    public static UsuarioRegistroDTO toRegistroDTO(Usuario u) {
        if (u == null) return null;

        return UsuarioRegistroDTO.builder()
                .username(u.getUsername())
                .password(u.getPassword_hash())
                .email(u.getEmail())
                .rol(u.getRol() != null ? u.getRol() : "SIN_ROL")
                .build();
    }
}
