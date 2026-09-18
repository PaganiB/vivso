package com.vivso.Vivso.Mapper;

import com.vivso.Vivso.DTO.*;
import com.vivso.Vivso.Model.*;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface VivsoMapper {

    // ======================== FAMILIA ========================
    // idFamilia coincide en entidad y DTO → sin @Mapping necesario

    FamiliaDTO toDTO(Familia familia);

    Familia toEntity(FamiliaDTO dto);

    @Mapping(target = "idFamilia", ignore = true)   // PK nunca se sobreescribe
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(FamiliaDTO dto, @MappingTarget Familia familia);


    // ======================== FAMILIAR ========================

    @Mapping(source = "familia.idFamilia",  target = "familia")
    FamiliarDTO toDTO(Familiar familiar);

    @Mapping(target = "familia",            ignore = true)
    @Mapping(target = "condicion_especial", defaultValue = "No posee discapacidad")
    Familiar toEntity(FamiliarDTO dto);

    @Mapping(target = "id_familiar",  ignore = true)    // PK
    @Mapping(target = "familia",      ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(FamiliarDTO dto, @MappingTarget Familiar familiar);


    // ======================== ORGANIZACION ========================
    // Todos los campos coinciden → sin @Mapping necesario

    OrganizacionDTO toDTO(Organizacion organizacion);

    Organizacion toEntity(OrganizacionDTO dto);

    @Mapping(target = "cuit", ignore = true)    // PK nunca cambia
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(OrganizacionDTO dto, @MappingTarget Organizacion organizacion);


    // ======================== INTEGRANTE ========================
    // idIntegrante coincide en entidad y DTO → sin @Mapping para la PK

    @Mapping(source = "organizacion.cuit", target = "cuitOrg")
    @Mapping(source = "usuario.username",  target = "usuario", defaultValue = "Sin usuario")
    IntegranteDTO toDTO(Integrante integrante);

    @Mapping(target = "organizacion", ignore = true)
    @Mapping(target = "usuario",      ignore = true)
    Integrante toEntity(IntegranteDTO dto);

    @Mapping(target = "idIntegrante", ignore = true)    // PK
    @Mapping(target = "organizacion", ignore = true)
    @Mapping(target = "usuario",      ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(IntegranteDTO dto, @MappingTarget Integrante integrante);


    // ======================== VIVIENDA ========================

    @Mapping(source = "familia.idFamilia",           target = "idFamilia")
    @Mapping(source = "familia.nombreRepresentante", target = "representante", defaultValue = "Sin asignar")
    ViviendaDTO toDTO(Vivienda vivienda);

    @Mapping(target = "familia", ignore = true)
    Vivienda toEntity(ViviendaDTO dto);

    @Mapping(target = "idVivienda", ignore = true)  // PK
    @Mapping(target = "familia",    ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(ViviendaDTO dto, @MappingTarget Vivienda vivienda);


    // ======================== SOLICITUD ========================

    @Mapping(source = "cuitOrg.cuit",               target = "cuitOrg")
    @Mapping(source = "familiaBeneficiaria.idFamilia", target = "idFamilia")
    @Mapping(source = "vivienda.idVivienda",        target = "idVivienda")
    SolicitudDTO toDTO(Solicitud solicitud);

    @Mapping(target = "cuitOrg",             ignore = true)
    @Mapping(target = "familiaBeneficiaria", ignore = true)
    @Mapping(target = "vivienda",            ignore = true)
    @Mapping(target = "fechaActivacion",     ignore = true)
    Solicitud toEntity(SolicitudDTO dto);


    // ======================== DOCUMENTO ========================
    // idDocumento, nombre y url coinciden en entidad y DTO → sin @Mapping para esos campos

    @Mapping(source = "familia.idFamilia",  target = "idFamilia")
    @Mapping(source = "organizacion.cuit",  target = "cuitOrg")
    @Mapping(source = "revisor.id",         target = "idUsuarioRevisor")
    @Mapping(source = "revisor.username",   target = "nombreRevisor", defaultValue = "Sin asignar")
    DocumentoDTO toDTO(Documento documento);


    // ======================== USUARIO ========================

    @Mapping(source = "rol", target = "rol", defaultValue = "SIN_ROL")
    UsuarioRespuestaDTO toRespuestaDTO(Usuario usuario);

    @Mapping(source = "password_hash", target = "password")
    @Mapping(source = "rol",           target = "rol", defaultValue = "SIN_ROL")
    UsuarioRegistroDTO toRegistroDTO(Usuario usuario);

    // ======================== VISITA OBRA ========================

    @Mapping(source = "vivienda.idVivienda", target = "idVivienda")
    @Mapping(source = "tecnico.id", target = "idTecnico")
    VisitaObraDTO toDTO(VisitaObra visitaObra);

    @Mapping(target = "vivienda", ignore = true)
    @Mapping(target = "tecnico", ignore = true)
    VisitaObra toEntity(VisitaObraDTO dto);

    @Mapping(target = "idVisitaObra", ignore = true) // PK intocable
    @Mapping(target = "vivienda", ignore = true)
    @Mapping(target = "tecnico", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(VisitaObraDTO dto, @MappingTarget VisitaObra visitaObra);

    // ======================== VISITA INICIAL ========================

    @Mapping(source = "solicitud.idSolicitud", target = "idSolicitud") // Extraemos el ID del objeto
    @Mapping(source = "tecnico.id",            target = "idTecnico")
    VisitaInicialDTO toDTO(VisitaInicial visitaInicial);

    @Mapping(target = "solicitud", ignore = true) // Se busca y setea en el Service
    @Mapping(target = "tecnico",   ignore = true) // Se busca y setea en el Service
    VisitaInicial toEntity(VisitaInicialDTO dto);

    @Mapping(target = "id",        ignore = true) // PK intocable
    @Mapping(target = "solicitud", ignore = true) // No modificamos la relación desde acá
    @Mapping(target = "tecnico",   ignore = true) // No modificamos la relación desde acá
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(VisitaInicialDTO dto, @MappingTarget VisitaInicial visitaInicial);

    // ======================== SOLICITUD ORGANIZACION ========================
    // Todos los campos coinciden → sin @Mapping necesario para el toDTO

    SolicitudOrganizacionDTO toDTO(SolicitudOrganizacion solicitudOrganizacion);

    @Mapping(target = "cuit",                              ignore = true) // No se edita vía PATCH
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromPatchDto(SolicitudOrganizacionPatchDTO dto, @MappingTarget SolicitudOrganizacion solicitudOrganizacion);

}
