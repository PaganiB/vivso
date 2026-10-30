package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.*;
import com.vivso.Vivso.Exception.RecursoNoEncontradoException;
import com.vivso.Vivso.Exception.ReglaDeNegocioException;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.*;
import com.vivso.Vivso.Repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SolicitudService implements ISolicitudService {

  @Autowired
  private ISolicitudRepository solicitudRepo;
  @Autowired
  private IViviendaRepository viviendaRepo;
  @Autowired
  private IFamiliaRepository familiaRepo;
  @Autowired
  private IFamiliarRepository familiarRepo;
  @Autowired
  private IDocumentoService documentoService;
  @Autowired
  private IVisitaInicialRepository visitaInicialRepository;
  @Autowired
  private VivsoMapper mapper;
  @Autowired
  private IIntegranteRepository integranteRepo;
  @Autowired
  private IUsuarioRepository usuarioRepo;
  @Autowired
  private IEtapaObraRepository etapaObraRepo;
  @Autowired
  private IFotoEtapaRepository fotoEtapaRepo;

  @Override
  @Transactional
  public SolicitudDTO registrarSolicitudFamilia(RegistroFamiliaDTO dto,
                                                MultiValueMap<String, MultipartFile> mapaArchivos) {

    // La organización sale de quién está logueado, no de lo que mande el cliente
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    Integrante integrante = integranteRepo.findByUsuario_Email(email)
      .orElseThrow(() -> new ReglaDeNegocioException(
        "El usuario autenticado no está asociado a ninguna organización",
        HttpStatus.FORBIDDEN));
    Organizacion org = integrante.getOrganizacion();

    Familia familia = familiaRepo.save(mapper.toEntity(dto.getFamilia()));

    if (dto.getFamiliares() != null) {
      for (FamiliarDTO familiarDTO : dto.getFamiliares()) {
        Familiar familiar = mapper.toEntity(familiarDTO);
        familiar.setFamilia(familia);
        familiarRepo.save(familiar);
      }
    }

    Solicitud solicitud = new Solicitud();
    solicitud.setCuitOrg(org);
    solicitud.setFamiliaBeneficiaria(familia);
    solicitud = solicitudRepo.save(solicitud);

    if (mapaArchivos != null) {
      mapaArchivos.forEach((clave, listaArchivos) -> {
        if (!clave.equals("datos") && !listaArchivos.isEmpty()) {
          try {
            TipoDocumento tipoDoc = TipoDocumento.valueOf(clave.toUpperCase());
            for (MultipartFile archivo : listaArchivos) {
              documentoService.subirDocumento(archivo, tipoDoc, familia.getIdFamilia(), null);
            }
          } catch (IllegalArgumentException e) {
            log.warn("Clave de documento ignorada: {}", clave);
          }
        }
      });
    }
    return mapper.toDTO(solicitud);
  }

  @Override
  @Transactional
  public SolicitudDTO asignarNumeroExpediente(Integer idSolicitud, String numExp) {
    Solicitud s = solicitudRepo.findById(idSolicitud)
      .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada: " + idSolicitud));

    if (numExp == null || numExp.isBlank())
      throw new ReglaDeNegocioException("El número de expediente no puede estar vacío", HttpStatus.BAD_REQUEST);

    s.setNumExp(numExp);
    return mapper.toDTO(solicitudRepo.save(s));
  }

  @Override
  @Transactional
  public SolicitudDTO aprobarSolicitudVivienda(Integer idSolicitud) {
    Solicitud s = solicitudRepo.findById(idSolicitud)
      .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada: " + idSolicitud));

    if (s.getEstado() == EstadoSolicitud.Aprobada)
      throw new ReglaDeNegocioException("La solicitud ya está aprobada", HttpStatus.CONFLICT);

    if (s.getNumExp() == null || s.getNumExp().isBlank())
      throw new ReglaDeNegocioException("La solicitud no tiene número de expediente asignado. Debe cargarlo antes de aprobar.", HttpStatus.CONFLICT);

    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    Usuario revisor = usuarioRepo.findByEmail(email)
      .orElseThrow(() -> new RecursoNoEncontradoException("Usuario autenticado no encontrado"));

    s.setEstado(EstadoSolicitud.Aprobada);
    s.setFechaActivacion(LocalDate.now());
    s.setRevisor(revisor);
    solicitudRepo.save(s);

    Vivienda vivienda = Vivienda.builder()
      .familia(s.getFamiliaBeneficiaria())
      .solicitud(s)
      .build();
    viviendaRepo.save(vivienda);

    return mapper.toDTO(s);
  }

  @Override
  @Transactional
  public void rechazarSolicitudVivienda(Integer idSolicitud, String motivo) {
    Solicitud s = solicitudRepo.findById(idSolicitud)
      .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada: " + idSolicitud));

    if (s.getEstado() == EstadoSolicitud.Aprobada)
      throw new ReglaDeNegocioException("No se puede rechazar una solicitud ya aprobada", HttpStatus.CONFLICT);

    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    Usuario revisor = usuarioRepo.findByEmail(email)
      .orElseThrow(() -> new RecursoNoEncontradoException("Usuario autenticado no encontrado"));

    s.setEstado(EstadoSolicitud.Rechazada);
    s.setObservacion(motivo);
    s.setRevisor(revisor);
    solicitudRepo.save(s);
  }

  @Override
  @Transactional
  public SolicitudDTO semiAprobarSolicitud(Integer idSolicitud, List<String> camposObservados, String motivo) {
    Solicitud s = solicitudRepo.findById(idSolicitud)
      .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada: " + idSolicitud));

    if (s.getEstado() == EstadoSolicitud.Aprobada)
      throw new ReglaDeNegocioException("No se puede modificar una solicitud ya aprobada", HttpStatus.CONFLICT);
    if (s.getEstado() == EstadoSolicitud.Rechazada)
      throw new ReglaDeNegocioException("No se puede modificar una solicitud ya rechazada", HttpStatus.CONFLICT);

    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    Usuario revisor = usuarioRepo.findByEmail(email)
      .orElseThrow(() -> new RecursoNoEncontradoException("Usuario autenticado no encontrado"));

    s.setEstado(EstadoSolicitud.SemiAprobada);
    s.setObservacion(motivo);
    s.setCamposObservados(String.join(",", camposObservados));
    s.setRevisor(revisor);
    return mapper.toDTO(solicitudRepo.save(s));
  }

  @Override
  @Transactional
  public void confirmarCorreccion(Integer idSolicitud) {
    Solicitud s = solicitudRepo.findById(idSolicitud)
      .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada: " + idSolicitud));

    if (s.getEstado() != EstadoSolicitud.SemiAprobada)
      throw new ReglaDeNegocioException("Esta solicitud no está en observaciones", HttpStatus.CONFLICT);

    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    Usuario usuarioAutenticado = usuarioRepo.findByEmail(email)
      .orElseThrow(() -> new RecursoNoEncontradoException("Usuario autenticado no encontrado"));

    Integrante integrante = integranteRepo.findByUsuario(usuarioAutenticado)
      .orElseThrow(() -> new ReglaDeNegocioException("El usuario no tiene un integrante asociado", HttpStatus.FORBIDDEN));

    if (!integrante.getOrganizacion().getCuit().equals(s.getCuitOrg().getCuit()))
      throw new ReglaDeNegocioException("No tenés permiso para editar esta solicitud", HttpStatus.FORBIDDEN);

    boolean quedanDocumentosSinCorregir = documentoService.listarPorFamilia(s.getFamiliaBeneficiaria().getIdFamilia())
      .stream()
      .anyMatch(d -> d.getEstado() == EstadoDocumento.A_CORREGIR);

    if (quedanDocumentosSinCorregir)
      throw new ReglaDeNegocioException(
        "Todavía hay documentos marcados para corregir sin reemplazar. Reemplazalos antes de enviar.",
        HttpStatus.CONFLICT);

    s.setEstado(EstadoSolicitud.Pendiente);
    s.setObservacion(null);
    s.setCamposObservados(null);
    solicitudRepo.save(s);

    log.info("Solicitud {} confirmada por el solicitante, vuelve a estado Pendiente", idSolicitud);
  }

  @Override
  public List<SolicitudDTO> getSolicitudes() {
    return solicitudRepo.findAll().stream().map(mapper::toDTO).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ExpedienteMovilDTO> getSolicitudesParaMovil() {

    // 1. Identidad segura desde el Token
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    Usuario tecnico = usuarioRepo.findByEmail(email)
      .orElseThrow(() -> new RecursoNoEncontradoException("Técnico no encontrado"));

    // 2. Buscamos de un solo golpe todo lo necesario (Filtrado y con Familia/ONG incluidas)
    List<EstadoSolicitud> estadosPermitidos = List.of(EstadoSolicitud.Aprobada, EstadoSolicitud.Pendiente);

    List<Solicitud> solicitudes = solicitudRepo.buscarSolicitudesParaMovil(tecnico.getId(), estadosPermitidos);

    // 3. Mapeamos directamente a DTO sin hacer consultas extra
    return solicitudes.stream().map(solicitud -> {
      String nombreFamilia = (solicitud.getFamiliaBeneficiaria() != null)
        ? solicitud.getFamiliaBeneficiaria().getNombreRepresentante()
        : "Sin asignar";

      String nombreOng = (solicitud.getCuitOrg() != null)
        ? solicitud.getCuitOrg().getNombre()
        : "Sin asignar";

      return ExpedienteMovilDTO.builder()
        .idSolicitud(solicitud.getIdSolicitud())
        .numExp(solicitud.getNumExp())
        .nombreRepresentanteFamilia(nombreFamilia)
        .nombreOrganizacion(nombreOng)
        .estado(solicitud.getEstado())
        .build();

    }).collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<ExpedienteWebDTO> obtenerDashboardWebTecnico() {

    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    Usuario tecnico = usuarioRepo.findByEmail(email)
      .orElseThrow(() -> new RecursoNoEncontradoException("Técnico no encontrado"));

    // 1. Un solo viaje para traer todas las solicitudes + Familias + ONGs
    List<Solicitud> solicitudes = solicitudRepo.buscarDashboardWebPorTecnico(tecnico.getId());

    if (solicitudes.isEmpty()) {
      return new ArrayList<>();
    }

    // 2. Extraemos los IDs de esta lista
    List<Integer> idsSolicitudes = solicitudes.stream()
      .map(Solicitud::getIdSolicitud)
      .collect(Collectors.toList());

    // 3. Dos viajes MASIVOS a la base de datos (reemplaza a decenas de viajes individuales)
    Set<Integer> idsConVivienda = viviendaRepo.findIdsConVivienda(idsSolicitudes);
    Set<Integer> idsConVisita = visitaInicialRepository.findIdsConVisita(idsSolicitudes);

    // 4. Mapeamos los DTOs usando las listas que ya tenemos en memoria (0 viajes extra)
    return solicitudes.stream().map(s -> {
      Familia familia = s.getFamiliaBeneficiaria();
      Organizacion org = s.getCuitOrg();
      Integer id = s.getIdSolicitud();

      String estadoRelevamiento;
      if (s.getEstado() == EstadoSolicitud.Aprobada) {
        // Fase de obra: Solo nos fijamos si el ID está en el Set de memoria
        estadoRelevamiento = idsConVivienda.contains(id)
          ? "En Construcción"
          : "Aprobada - Esperando inicio";
      } else {
        // Fase inicial: Solo nos fijamos si el ID NO está en el Set de memoria
        estadoRelevamiento = !idsConVisita.contains(id)
          ? "Sin relevamiento"
          : "Relevamiento en proceso";
      }

      return ExpedienteWebDTO.builder()
        .idSolicitud(id)
        .numExp(s.getNumExp())
        .estadoSolicitud(s.getEstado())
        .estadoRelevamiento(estadoRelevamiento)
        .beneficiario(familia != null ? familia.getNombreRepresentante() : "Sin asignar")
        .dni(familia != null && familia.getDniRepresentante() != null
          ? familia.getDniRepresentante() : "S/D")
        .nombreOrg(org != null ? org.getNombre() : null)
        .cuitOrg(org != null ? org.getCuit() : null)
        .build();
    }).collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<ExpedienteMovilDTO> getObrasAsignadasParaOng() {

    // 1. Identidad segura desde el Token
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    Usuario usuario = usuarioRepo.findByEmail(email)
      .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

    // 2. Buscar al integrante para conocer a qué ONG pertenece
    Integrante integrante = integranteRepo.findByUsuario(usuario)
      .orElseThrow(() -> new ReglaDeNegocioException("El usuario autenticado no está asociado a ninguna organización", HttpStatus.FORBIDDEN));

    String cuitOng = integrante.getOrganizacion().getCuit();

    // 3. Un solo viaje a SFO: Trae solo las obras que tienen etapas y empaqueta a la familia
    List<Solicitud> obrasAsignadas = solicitudRepo.buscarObrasConEtapasParaOng(cuitOng);

    // 4. Mapear directamente a DTO sin consultas extra a la base de datos
    return obrasAsignadas.stream().map(solicitud -> {
      String nombreFamilia = (solicitud.getFamiliaBeneficiaria() != null)
        ? solicitud.getFamiliaBeneficiaria().getNombreRepresentante()
        : "Sin asignar";

      return ExpedienteMovilDTO.builder()
        .idSolicitud(solicitud.getIdSolicitud())
        .numExp(solicitud.getNumExp())
        .nombreRepresentanteFamilia(nombreFamilia)
        .nombreOrganizacion(integrante.getOrganizacion().getNombre())
        .estado(solicitud.getEstado())
        .build();
    }).collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public ExpedienteDetalleDTO obtenerExpedienteDetalle(Integer idSolicitud) {

    // 1. Buscamos la solicitud principal y validamos permisos del técnico
    Solicitud s = solicitudRepo.findById(idSolicitud)
      .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada: " + idSolicitud));

    validarAccesoTecnico(s);

    Familia familia = s.getFamiliaBeneficiaria();
    Organizacion org = s.getCuitOrg();

    // 2. Obtenemos integrantes de la organización
    List<Integrante> integrantes = org != null
      ? integranteRepo.findByOrganizacion_Cuit(org.getCuit())
      : List.of();

    // 3. Mapeamos el grupo familiar
    List<ExpedienteDetalleDTO.MiembroFamilia> grupoFamiliar = familia == null ? List.of()
      : familiarRepo.findByFamilia_IdFamilia(familia.getIdFamilia()).stream()
      .map(f -> ExpedienteDetalleDTO.MiembroFamilia.builder()
        .dni(f.getDni())
        .nombre(f.getNombre())
        .apellido(f.getApellido())
        .discapacidad(tieneDiscapacidad(f.getCondicion_especial()))
        .build())
      .toList();

    // 4. OPTIMIZACIÓN N+1: Traemos etapas y TODAS sus fotos en bloque (solo 2 viajes a la BD)
    List<EtapaObra> etapasObra = etapaObraRepo.findBySolicitud_IdSolicitud(idSolicitud);
    List<Integer> idsEtapas = etapasObra.stream().map(EtapaObra::getIdEtapaObra).toList();

    List<FotoEtapa> todasLasFotos = idsEtapas.isEmpty() ? List.of() :
      fotoEtapaRepo.findByEtapa_IdEtapaObraIn(idsEtapas);

    // Agrupamos las fotos por ID de etapa en memoria (eficiencia máxima)
    Map<Integer, List<FotoEtapa>> fotosPorEtapa = todasLasFotos.stream()
      .collect(Collectors.groupingBy(f -> f.getEtapa().getIdEtapaObra()));

    // Construimos la lista de etapas con sus respectivas fotos
    List<ExpedienteDetalleDTO.EtapaConFotos> etapas = etapasObra.stream()
      .map(e -> ExpedienteDetalleDTO.EtapaConFotos.builder()
        .etapa(mapper.toDTO(e))
        .fotos(fotosPorEtapa.getOrDefault(e.getIdEtapaObra(), List.of())
          .stream().map(mapper::toDTO).toList())
        .build())
      .toList();

    // 5. Ensamblaje y retorno del DTO principal con Builder
    return ExpedienteDetalleDTO.builder()
      .id(s.getIdSolicitud())
      .numero(s.getNumExp() != null ? s.getNumExp() : "Sin asignar")
      .estado(s.getEstado().name())
      .beneficiario(ExpedienteDetalleDTO.Beneficiario.builder()
        .nombre(familia != null ? familia.getNombreRepresentante() : "Sin asignar")
        .dni(familia != null && familia.getDniRepresentante() != null ? familia.getDniRepresentante() : "S/D")
        .build())
      .ong(org == null ? null : ExpedienteDetalleDTO.Ong.builder()
        .nombre(org.getNombre()).cuit(org.getCuit()).build())
      .org(org == null ? null : ExpedienteDetalleDTO.Org.builder()
        .cuit(org.getCuit())
        .nombre(org.getNombre())
        .tipo(org.getTipo())
        .domicilioLegal(org.getDom_legal())
        .nroCpe(org.getCpe())
        .vencimientoVigencia(org.getFechaVencimientoVigencia())
        .build())
      .presidente(autoridad(integrantes, TipoCargo.PRESIDENTE))
      .tesorero(autoridad(integrantes, TipoCargo.TESORERO))
      .solicitud(ExpedienteDetalleDTO.SolicitudDatos.builder()
        .representanteNombre(familia != null ? familia.getNombreRepresentante() : null)
        .representanteTelefono(familia != null ? familia.getContacto() : null)
        .fechaSolicitud(s.getFechaSolicitud())
        .observaciones(s.getObservacion())
        .grupoFamiliar(grupoFamiliar)
        .antiguedadPosesion(familia != null ? familia.getAntiguedadRancho() : null)
        .ubicacion(familia != null ? familia.getCoordenadasRancho() : null)
        .build())
      .visitas(visitaInicialRepository.findBySolicitud_IdSolicitudOrderByFechaVisitaDesc(idSolicitud)
        .stream().map(mapper::toDTO).toList())
      .vivienda(viviendaRepo.findBySolicitud_IdSolicitud(idSolicitud)
        .map(mapper::toDTO).orElse(null))
      .etapas(etapas)
      .build();
  }

  @Override
  @Transactional
  public void asignarTecnico(Integer idSolicitud, Integer idTecnico) {

    // 1. Buscamos la solicitud
    Solicitud solicitud = solicitudRepo.findById(idSolicitud)
      .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada: " + idSolicitud));

    // 2. Controlar que NO tenga una visita inicial registrada
    boolean yaTieneVisita = visitaInicialRepository.existsBySolicitud_IdSolicitud(idSolicitud);
    if (yaTieneVisita) {
      throw new ReglaDeNegocioException(
        "No se puede asignar o reasignar un técnico porque este expediente ya cuenta con una visita preliminar registrada.",
        HttpStatus.CONFLICT
      );
    }

    // 3. Buscamos al usuario que queremos asignar
    Usuario nuevoTecnico = usuarioRepo.findById(idTecnico)
      .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + idTecnico));

    // 4. Validamos que el usuario tenga el rol de técnico
    if (!"TECNICO".equals(nuevoTecnico.getRol())) {
      throw new ReglaDeNegocioException("El usuario seleccionado no tiene el rol de técnico.", HttpStatus.CONFLICT);
    }

    // 5. Asignamos y guardamos
    solicitud.setTecnicoAsignado(nuevoTecnico);
    solicitudRepo.save(solicitud);
  }

  // El técnico solo ve los expedientes que tiene asignados; ADMIN, OPERADOR y ARQUITECTO ven todos
  private void validarAccesoTecnico(Solicitud s) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    boolean esTecnico = auth.getAuthorities().stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_TECNICO"));
    if (!esTecnico) return;

    Usuario tecnico = usuarioRepo.findByEmail(auth.getName())
      .orElseThrow(() -> new RecursoNoEncontradoException("Técnico no encontrado"));
    Usuario asignado = s.getTecnicoAsignado();
    if (asignado == null || !Objects.equals(asignado.getId(), tecnico.getId())) {
      throw new ReglaDeNegocioException("No tenés asignado este expediente", HttpStatus.FORBIDDEN);
    }
  }

  private ExpedienteDetalleDTO.Autoridad autoridad(List<Integrante> integrantes, TipoCargo cargo) {
    return integrantes.stream()
      .filter(i -> i.getCargo() == cargo && Boolean.TRUE.equals(i.getActivo()))
      .findFirst()
      .map(i -> ExpedienteDetalleDTO.Autoridad.builder()
        .nombre(i.getNombre()).apellido(i.getApellido()).dni(i.getDni())
        .email(i.getCorreo()).telefono(i.getTelefono()).domicilio(i.getDomicilio())
        .fechaAltaCargo(i.getFechaAltaCargo())
        .build())
      .orElse(null);
  }

  // La base guarda un texto; el front quiere un sí/no
  private boolean tieneDiscapacidad(String condicion) {
    return condicion != null && !condicion.isBlank()
      && !condicion.equalsIgnoreCase("No posee discapacidad");
  }

  @Override
  public SolicitudDTO findSolicitud(Integer id) {
    return solicitudRepo.findById(id)
      .map(mapper::toDTO)
      .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada: " + id));
  }

  @Override
  public List<SolicitudDTO> buscarPorEstado(EstadoSolicitud estado) {
    List<Solicitud> result = solicitudRepo.findByEstado(estado);
    return result.stream().map(mapper::toDTO).toList();
  }

  @Override
  public List<SolicitudDTO> buscarPorOrganizacion(String cuitOrg) {
    List<Solicitud> result = solicitudRepo.findByCuitOrg_Cuit(cuitOrg);
    return result.stream().map(mapper::toDTO).toList();
  }

  @Override
  public void deleteSolicitud(Integer id) {
    Solicitud s = solicitudRepo.findById(id)
      .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada: " + id));
    solicitudRepo.delete(s);
  }
}