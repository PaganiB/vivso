package com.vivso.Vivso.Mapper;

import com.vivso.Vivso.DTO.*;
import com.vivso.Vivso.Model.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(
  componentModel = "spring",
  unmappedSourcePolicy = ReportingPolicy.IGNORE,
  unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface VivsoMapper {

  // ======================== FAMILIA ========================

  // DTO de respuesta
  FamiliaDTO toDTO(Familia familia);

  // DTO de registro
  Familia toEntity(FamiliaRegistroDTO dto);

  // Se mantienen los métodos existentes
  Familia toEntity(FamiliaDTO dto);

  @Mapping(target = "idFamilia", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateFromDto(FamiliaDTO dto, @MappingTarget Familia familia);


// ======================== FAMILIAR ========================

  // DTO de respuesta
  @Mapping(source = "familia.idFamilia", target = "familia")
  FamiliarDTO toDTO(Familiar familiar);

  // DTO de registro
  @Mapping(target = "id_familiar", ignore = true)
  @Mapping(target = "familia", ignore = true)
  @Mapping(target = "condicion_especial", defaultValue = "No posee discapacidad")
  Familiar toEntity(FamiliarRegistroDTO dto);

  // Se mantienen los métodos existentes
  @Mapping(target = "familia", ignore = true)
  @Mapping(target = "condicion_especial", defaultValue = "No posee discapacidad")
  Familiar toEntity(FamiliarDTO dto);

  @Mapping(target = "id_familiar", ignore = true)
  @Mapping(target = "familia", ignore = true)
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

  // DTO de respuesta
  @Mapping(source = "organizacion.cuit", target = "cuitOrg")
  @Mapping(source = "usuario.username", target = "usuario", defaultValue = "Sin usuario")
  IntegranteDTO toDTO(Integrante integrante);

  // DTO de registro
  @Mapping(target = "idIntegrante", ignore = true)
  @Mapping(target = "organizacion", ignore = true)
  @Mapping(target = "usuario", ignore = true)
  @Mapping(target = "activo", ignore = true)
  @Mapping(target = "cargo", ignore = true)
  Integrante toEntity(IntegranteRegistroDTO dto);

  // DTO anterior (lo mantenemos por ahora)
  @Mapping(target = "organizacion", ignore = true)
  @Mapping(target = "usuario", ignore = true)
  Integrante toEntity(IntegranteDTO dto);

  @Mapping(target = "idIntegrante", ignore = true)
  @Mapping(target = "organizacion", ignore = true)
  @Mapping(target = "usuario", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateFromDto(IntegranteDTO dto, @MappingTarget Integrante integrante);


  // ======================== VIVIENDA ========================

  @Mapping(source = "familia.idFamilia", target = "idFamilia")
  @Mapping(source = "solicitud.idSolicitud", target = "solicitud")
  @Mapping(source = "familia.nombreRepresentante", target = "representante", defaultValue = "Sin asignar")
  ViviendaDTO toDTO(Vivienda vivienda);

  @Mapping(target = "idVivienda", ignore = true)
  @Mapping(target = "familia", ignore = true)
  @Mapping(target = "solicitud", ignore = true)
  Vivienda toEntity(ViviendaRegistroDTO dto);

  @Mapping(target = "idVivienda", ignore = true)
  @Mapping(target = "familia", ignore = true)
  @Mapping(target = "solicitud", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateFromDto(ViviendaRegistroDTO dto, @MappingTarget Vivienda vivienda);

  // ======================== SOLICITUD ========================

  @Mapping(source = "cuitOrg.cuit", target = "cuitOrg")
  @Mapping(source = "cuitOrg.nombre", target = "nombreOrganizacion")
  @Mapping(source = "familiaBeneficiaria", target = "familia")
  @Mapping(source = "familiaBeneficiaria.idFamilia", target = "idFamilia")
  @Mapping(source = "tecnicoAsignado.id", target = "idTecnico")
  SolicitudDTO toDTO(Solicitud solicitud);

  @Mapping(target = "cuitOrg", ignore = true)
  @Mapping(target = "familiaBeneficiaria", ignore = true)
  @Mapping(target = "fechaActivacion", ignore = true)
  @Mapping(target = "revisor", ignore = true)
  @Mapping(target = "tecnicoAsignado", ignore = true)
  Solicitud toEntity(SolicitudDTO dto);


  // ======================== DOCUMENTO ========================
  // idDocumento, nombre y url coinciden en entidad y DTO → sin @Mapping para esos campos

  @Mapping(source = "familia.idFamilia", target = "idFamilia")
  @Mapping(source = "familiar.id_familiar", target = "idFamiliar")
  @Mapping(source = "organizacion.cuit", target = "cuitOrg")
  @Mapping(source = "revisor.id", target = "idUsuarioRevisor")
  @Mapping(source = "revisor.username", target = "nombreRevisor", defaultValue = "Sin asignar")
  DocumentoDTO toDTO(Documento documento);


  // ======================== USUARIO ========================

  @Mapping(source = "rol", target = "rol", defaultValue = "SIN_ROL")
  UsuarioRespuestaDTO toRespuestaDTO(Usuario usuario);

  @Mapping(source = "password_hash", target = "password")
  @Mapping(source = "rol", target = "rol", defaultValue = "SIN_ROL")
  UsuarioRegistroDTO toRegistroDTO(Usuario usuario);


  // ======================== VISITA INICIAL ========================

  @Mapping(source = "solicitud.idSolicitud", target = "idSolicitud") // Extraemos el ID del objeto
  @Mapping(source = "tecnico.id", target = "idTecnico")
  VisitaInicialDTO toDTO(VisitaInicial visitaInicial);

  @Mapping(target = "solicitud", ignore = true) // Se busca y setea en el Service
  @Mapping(target = "tecnico", ignore = true)
    // Se busca y setea en el Service
  VisitaInicial toEntity(VisitaInicialDTO dto);

  @Mapping(target = "id", ignore = true) // PK intocable
  @Mapping(target = "solicitud", ignore = true) // No modificamos la relación desde acá
  @Mapping(target = "tecnico", ignore = true) // No modificamos la relación desde acá
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateFromDto(VisitaInicialDTO dto, @MappingTarget VisitaInicial visitaInicial);

  // ======================== SOLICITUD ORGANIZACION ========================

  SolicitudOrganizacionDTO toDTO(SolicitudOrganizacion solicitudOrganizacion);

  @Mapping(target = "cuit", ignore = true) // No se edita vía PATCH
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateFromPatchDto(SolicitudOrganizacionPatchDTO dto, @MappingTarget SolicitudOrganizacion solicitudOrganizacion);

  // ======================== ETAPA OBRA ========================
  @Mapping(source = "solicitud.idSolicitud", target = "idSolicitud")
  @Mapping(source = "solicitud.numExp", target = "numExp")
  @Mapping(source = "etapa.numero", target = "numeroEtapa")
  @Mapping(source = "etapa.nombre", target = "nombreEtapa")
  @Mapping(source = "etapa.descripcion", target = "descripcionEtapa")
  @Mapping(source = "etapa.diasEstimados", target = "diasEstimados")
  @Mapping(source = "enviadoPor.id", target = "idUsuarioEnvio")
  @Mapping(source = "enviadoPor.username", target = "nombreEnvio", defaultValue = "Sin enviar")
  @Mapping(source = "revisadoPor.id", target = "idUsuarioRevisor")
  @Mapping(source = "revisadoPor.username", target = "nombreRevisor", defaultValue = "Sin revisar")
  EtapaObraDTO toDTO(EtapaObra etapa);

  // ======================== FOTO ETAPA ========================
  @Mapping(source = "etapa.idEtapaObra", target = "idEtapa")
  @Mapping(source = "subidaPor.id", target = "idUsuarioSubida")
  @Mapping(source = "subidaPor.username", target = "nombreUsuarioSubida", defaultValue = "Desconocido")
  FotoEtapaDTO toDTO(FotoEtapa foto);

  // ======================== NOTIFICACION ========================
  // idNotificacion, titulo, mensaje, fecha, leida, tipo e idReferencia coinciden exactamente → sin @Mapping necesario

  NotificacionDTO toDTO(Notificacion notificacion);

  List<NotificacionDTO> toDTOList(List<Notificacion> notificaciones);
}
