package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.*;
import com.vivso.Vivso.Exception.ReglaDeNegocioException;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.*;
import com.vivso.Vivso.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EtapaObraService implements IEtapaObraService {

  // El servidor puede estar en UTC: sin esto, "hoy" cambiaría a las 21 hs de Argentina
  private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
  private static final int TOPE_PRORROGA_DIAS = 14;

  @Autowired
  private IEtapaObraRepository etapaRepo;
  @Autowired
  private ISolicitudRepository solicitudRepo;
  @Autowired
  private IUsuarioRepository usuarioRepo;
  @Autowired
  private IIntegranteRepository integranteRepo;
  @Autowired
  private CalendarioService calendario;
  @Autowired
  private VivsoMapper mapper;
  @Autowired
  private IFotoEtapaRepository fotoRepo;
  @Autowired
  private NotificacionService notificacionService;

  // ======================== INICIAR OBRA (técnico asignado) ========================

  // Crea las 6 etapas del expediente y habilita la primera (ahí empieza a correr su plazo).
  @Transactional
  public List<EtapaObraDTO> iniciarObra(Integer idSolicitud, String emailTecnico) {
    Solicitud solicitud = buscarSolicitud(idSolicitud);
    validarTecnicoAsignado(solicitud, emailTecnico);

    if (solicitud.getEstado() != EstadoSolicitud.Aprobada) {
      throw conflicto("Solo se puede iniciar la obra de una solicitud aprobada.");
    }
    if (etapaRepo.existsBySolicitud_IdSolicitud(idSolicitud)) {
      throw conflicto("La obra de este expediente ya fue iniciada.");
    }

    List<EtapaObra> etapas = new ArrayList<>();
    for (EtapaConstruccion tipo : EtapaConstruccion.values()) {
      etapas.add(EtapaObra.builder()
        .solicitud(solicitud)
        .etapa(tipo)
        .estado(EstadoEtapa.PENDIENTE)
        .build());
    }
    habilitar(etapas.get(0));
    etapaRepo.saveAll(etapas);
    // NOTIFICAR A LA ONG QUE LA OBRA INICIÓ Y LA ETAPA ESTÁ HABILITADA
    notificacionService.notificarOng(
      solicitud.getCuitOrg().getCuit(),
      "Obra Iniciada",
      "Se habilitó la primera etapa del expediente " + solicitud.getNumExp() + ". ¡Ya pueden registrar avances!",
      "ETAPA_HABILITADA",
      solicitud.getIdSolicitud()
    );
    return aDTOs(etapas);
  }

  // ======================== LISTAR ========================

  @Transactional(readOnly = true)
  public List<EtapaObraDTO> listarEtapas(Integer idSolicitud, String email, boolean esAdmin) {
    Solicitud solicitud = buscarSolicitud(idSolicitud);
    if (!esAdmin && !esTecnicoAsignado(solicitud, email) && !esIntegranteDeLaOrganizacion(solicitud, email)) {
      throw prohibido("No tenés permiso para ver las etapas de este expediente.");
    }

    // 1. Primer viaje: Buscamos las etapas (consulta normal)
    List<EtapaObra> etapas = etapaRepo.findBySolicitud_IdSolicitud(idSolicitud);

    if (etapas.isEmpty()) {
      return new ArrayList<>();
    }

    // 2. Extraemos los IDs de las etapas
    List<Integer> idsEtapas = etapas.stream()
      .map(EtapaObra::getIdEtapaObra)
      .collect(Collectors.toList());

    // 3. SEGUNDO VIAJE (Masivo): Buscamos TODAS las fotos de esas etapas de un solo golpe
    List<FotoEtapa> todasLasFotos = fotoRepo.findByEtapa_IdEtapaObraIn(idsEtapas);

    // 4. Agrupamos las fotos por ID de Etapa usando la memoria RAM (0 viajes a la BD)
    Map<Integer, List<FotoEtapa>> fotosPorEtapa = todasLasFotos.stream()
      .collect(Collectors.groupingBy(foto -> foto.getEtapa().getIdEtapaObra()));

    // 5. Mapeamos a DTO inyectando las fotos desde el Map
    return etapas.stream()
      .sorted(Comparator.comparingInt(e -> e.getEtapa().getNumero()))
      .map(etapa -> {
        EtapaObraDTO dto = mapper.toDTO(etapa);

        // Sacamos las fotos de esta etapa específica desde nuestro Map en memoria
        List<FotoEtapa> fotosDeEstaEtapa = fotosPorEtapa.getOrDefault(etapa.getIdEtapaObra(), new ArrayList<>());

        if (!fotosDeEstaEtapa.isEmpty()) {
          List<FotoEtapaDTO> fotosDTO = fotosDeEstaEtapa.stream().map(foto -> {
            FotoEtapaDTO fDto = new FotoEtapaDTO();
            fDto.setIdFoto(foto.getIdFoto());
            fDto.setIdEtapa(etapa.getIdEtapaObra());
            fDto.setRutaArchivo(foto.getRutaArchivo());
            fDto.setNombreArchivo(foto.getNombreArchivo());
            fDto.setFechaSubida(foto.getFechaSubida());
            fDto.setIdUsuarioSubida(foto.getSubidaPor() != null ? foto.getSubidaPor().getId() : null);
            fDto.setNombreUsuarioSubida(foto.getSubidaPor() != null ? foto.getSubidaPor().getUsername() : null);
            return fDto;
          }).collect(Collectors.toList());

          dto.setFotos(fotosDTO);
        }

        return dto;
      })
      .collect(Collectors.toList());
  }

  // ======================== ENVIAR (integrante de la ONG) ========================

  @Transactional
  public EtapaObraDTO enviarEtapa(Integer idEtapa, EnviarEtapaDTO dto, String email) {
    EtapaObra etapa = buscarEtapa(idEtapa);
    if (!esIntegranteDeLaOrganizacion(etapa.getSolicitud(), email)) {
      throw prohibido("Solo un integrante de la organización del expediente puede enviar la etapa.");
    }
    if (etapa.getEstado() != EstadoEtapa.EN_CURSO &&
      etapa.getEstado() != EstadoEtapa.OBSERVADA &&
      etapa.getEstado() != EstadoEtapa.PRORROGADA) {
      throw conflicto("Solo se puede enviar una etapa en curso, observada o prorrogada. Estado actual: " + etapa.getEstado());
    }
    // TODO: exigir al menos una foto cargada cuando exista FotoEtapa

    etapa.setEstado(EstadoEtapa.EN_REVISION);
    etapa.setFechaEnvio(LocalDateTime.now(ZONA));
    etapa.setEnviadoPor(buscarUsuario(email));
    etapa.setObservacionOrganizacion(dto != null ? dto.getObservacionOrganizacion() : null);

    // Notificación Persistente + Push individual al Técnico
    Usuario tecnico = etapa.getSolicitud().getTecnicoAsignado();
    if (tecnico != null) {
      notificacionService.notificarUsuario(
        tecnico,
        "Etapa Recibida",
        "La ONG envió una etapa a revisión en el expediente " + etapa.getSolicitud().getNumExp() + ".",
        "ETAPA_ENVIADA",
        etapa.getSolicitud().getIdSolicitud()
      );
    }

    return mapper.toDTO(etapaRepo.save(etapa));
  }

  // ======================== APROBAR (técnico asignado) ========================

  @Transactional
  public EtapaObraDTO aprobarEtapa(Integer idEtapa, String email) {
    EtapaObra etapa = buscarEtapa(idEtapa);
    validarTecnicoAsignado(etapa.getSolicitud(), email);
    exigirEstado(etapa, EstadoEtapa.EN_REVISION, "aprobar");

    etapa.setEstado(EstadoEtapa.APROBADA);
    etapa.setRevisadoPor(buscarUsuario(email));
    etapa.setFechaRevision(LocalDateTime.now(ZONA));

    // Habilita la siguiente: desde este momento corre su plazo
    EtapaConstruccion siguiente = etapa.getEtapa().obtenerSiguienteEtapa();
    if (siguiente != null) {
      EtapaObra proxima = etapaRepo
        .findBySolicitud_IdSolicitudAndEtapa(etapa.getSolicitud().getIdSolicitud(), siguiente)
        .orElseThrow(() -> noEncontrado("No se encontró la etapa siguiente del expediente."));
      habilitar(proxima);
      etapaRepo.save(proxima);
    }
    // Notificación Persistente + Push a toda la ONG
    notificacionService.notificarOng(
      etapa.getSolicitud().getCuitOrg().getCuit(),
      "Etapa Aprobada",
      "El técnico aprobó una etapa del expediente " + etapa.getSolicitud().getNumExp() + ".",
      "ETAPA_APROBADA",
      etapa.getSolicitud().getIdSolicitud()
    );
    return mapper.toDTO(etapaRepo.save(etapa));
  }

  // ======================== OBSERVAR (técnico asignado) ========================

  @Transactional
  public EtapaObraDTO observarEtapa(Integer idEtapa, ObservarEtapaDTO dto, String email) {
    EtapaObra etapa = buscarEtapa(idEtapa);
    validarTecnicoAsignado(etapa.getSolicitud(), email);
    exigirEstado(etapa, EstadoEtapa.EN_REVISION, "observar");

    etapa.setEstado(EstadoEtapa.OBSERVADA);
    etapa.setObservacionTecnico(dto.getObservacionTecnico());
    etapa.setRevisadoPor(buscarUsuario(email));
    etapa.setFechaRevision(LocalDateTime.now(ZONA));

    // Notificación Persistente + Push
    notificacionService.notificarOng(
      etapa.getSolicitud().getCuitOrg().getCuit(),
      "Etapa Observada",
      "El técnico dejó observaciones en el expediente " + etapa.getSolicitud().getNumExp() + ".",
      "ETAPA_OBSERVADA",
      etapa.getSolicitud().getIdSolicitud()
    );

    return mapper.toDTO(etapaRepo.save(etapa));
  }

  // ======================== PRÓRROGA (técnico asignado) ========================

  @Transactional
  public EtapaObraDTO prorrogarEtapa(Integer idEtapa, ProrrogaEtapaDTO dto, String email) {
    EtapaObra etapa = buscarEtapa(idEtapa);
    validarTecnicoAsignado(etapa.getSolicitud(), email);

    if (etapa.getEstado() != EstadoEtapa.EN_CURSO &&
      etapa.getEstado() != EstadoEtapa.OBSERVADA &&
      etapa.getEstado() != EstadoEtapa.EN_REVISION &&
      etapa.getEstado() != EstadoEtapa.PRORROGADA) {
      throw conflicto("Solo se puede prorrogar una etapa en curso, observada prorrogada, o en revisión. Estado actual: " + etapa.getEstado());
    }

    LocalDate nueva = dto.getNuevaFechaLimite();

    // 1. Validar que la prórroga sume tiempo (no puede ser menor o igual al vencimiento actual)
    if (!nueva.isAfter(etapa.getFechaLimite())) {
      throw conflicto("La nueva fecha debe ser posterior al vencimiento actual (" + etapa.getFechaLimite() + ").");
    }

    // 2. Validar que el técnico no haya seleccionado un fin de semana o feriado por error
    if (!calendario.esDiaHabil(nueva)) {
      throw conflicto("La nueva fecha de vencimiento debe ser un día hábil.");
    }

    etapa.setFechaLimite(nueva);
    etapa.setMotivoProrroga(dto.getMotivoProrroga());
    etapa.setEstado(EstadoEtapa.PRORROGADA);

    // Notificación Persistente + Push
    notificacionService.notificarOng(
      etapa.getSolicitud().getCuitOrg().getCuit(),
      "Prórroga Otorgada",
      "Se extendió la fecha límite del expediente " + etapa.getSolicitud().getNumExp() + " hasta el " + nueva + ".",
      "ETAPA_PRORROGADA",
      etapa.getSolicitud().getIdSolicitud()
    );

    return mapper.toDTO(etapaRepo.save(etapa));
  }

  // ======================== RECHAZAR (técnico asignado) ========================
  @Transactional
  public EtapaObraDTO rechazarEtapa(Integer idEtapa, RechazarEtapaDTO dto, String email) {
    EtapaObra etapa = buscarEtapa(idEtapa);

    validarTecnicoAsignado(etapa.getSolicitud(), email);
    exigirEstado(etapa, EstadoEtapa.EN_REVISION, "rechazar");

    etapa.setEstado(EstadoEtapa.RECHAZADA);

    etapa.setObservacionTecnico(dto.getMotivoRechazo());

    etapa.setRevisadoPor(buscarUsuario(email));
    etapa.setFechaRevision(LocalDateTime.now(ZONA));

    // Notificación Persistente + Push
    notificacionService.notificarOng(
      etapa.getSolicitud().getCuitOrg().getCuit(),
      "Etapa Rechazada",
      "El técnico rechazó la etapa enviada en el expediente " + etapa.getSolicitud().getNumExp() + ".",
      "ETAPA_RECHAZADA",
      etapa.getSolicitud().getIdSolicitud()
    );

    return mapper.toDTO(etapaRepo.save(etapa));
  }

  // ======================== INTERNOS ========================

  // Un solo lugar para elegir el código HTTP de cada tipo de error
  private ReglaDeNegocioException conflicto(String mensaje) {
    return new ReglaDeNegocioException(mensaje, HttpStatus.CONFLICT);      // 409
  }

  private ReglaDeNegocioException prohibido(String mensaje) {
    return new ReglaDeNegocioException(mensaje, HttpStatus.FORBIDDEN);     // 403
  }

  private ReglaDeNegocioException noEncontrado(String mensaje) {
    return new ReglaDeNegocioException(mensaje, HttpStatus.NOT_FOUND);     // 404
  }

  // Habilitar = la etapa pasa a EN_CURSO y empieza a correr su plazo en días hábiles
  private void habilitar(EtapaObra etapa) {
    LocalDate hoy = LocalDate.now(ZONA);
    LocalDate limite = calendario.sumarDiasHabiles(hoy, etapa.getEtapa().getDiasEstimados());
    etapa.setEstado(EstadoEtapa.EN_CURSO);
    etapa.setFechaInicio(hoy);
    etapa.setFechaLimite(limite);
    etapa.setFechaLimiteOriginal(limite);
  }

  private void exigirEstado(EtapaObra etapa, EstadoEtapa esperado, String accion) {
    if (etapa.getEstado() != esperado) {
      throw conflicto("Solo se puede " + accion + " una etapa en estado " + esperado
        + ". Estado actual: " + etapa.getEstado());
    }
  }

  private boolean esTecnicoAsignado(Solicitud solicitud, String email) {
    Usuario tecnico = solicitud.getTecnicoAsignado();
    return tecnico != null && Objects.equals(tecnico.getEmail(), email);
  }

  private void validarTecnicoAsignado(Solicitud solicitud, String email) {
    if (!esTecnicoAsignado(solicitud, email)) {
      throw prohibido("Solo el técnico asignado a este expediente puede realizar esta acción.");
    }
  }

  private boolean esIntegranteDeLaOrganizacion(Solicitud solicitud, String email) {
    return integranteRepo.findByUsuario_Email(email)
      .map(i -> Objects.equals(i.getOrganizacion().getCuit(), solicitud.getCuitOrg().getCuit()))
      .orElse(false);
  }

  private Solicitud buscarSolicitud(Integer idSolicitud) {
    return solicitudRepo.findById(idSolicitud)
      .orElseThrow(() -> noEncontrado("No se encontró la solicitud " + idSolicitud));
  }

  private EtapaObra buscarEtapa(Integer idEtapa) {
    return etapaRepo.findById(idEtapa)
      .orElseThrow(() -> noEncontrado("No se encontró la etapa " + idEtapa));
  }

  private Usuario buscarUsuario(String email) {
    return usuarioRepo.findByEmail(email)
      .orElseThrow(() -> noEncontrado("No se encontró el usuario autenticado."));
  }

  // Ordena por número de etapa (1..6) y mapea; hay que hacerlo dentro de la transacción (relaciones LAZY)
  private List<EtapaObraDTO> aDTOs(List<EtapaObra> etapas) {
    if (etapas == null || etapas.isEmpty()) {
      return new ArrayList<>();
    }

    // 1. Extraemos los IDs de las etapas que vamos a procesar
    List<Integer> idsEtapas = etapas.stream()
      .map(EtapaObra::getIdEtapaObra)
      .collect(Collectors.toList());

    // 2. VIAJE MASIVO: Buscamos TODAS las fotos de estas etapas de un solo golpe
    // (Asegurate de tener inyectado: @Autowired private IFotoEtapaRepository fotoRepo;)
    List<FotoEtapa> todasLasFotos = fotoRepo.findByEtapa_IdEtapaObraIn(idsEtapas);

    // 3. Agrupamos las fotos por ID de etapa usando la memoria RAM (0 viajes extra)
    Map<Integer, List<FotoEtapa>> fotosPorEtapa = todasLasFotos.stream()
      .collect(Collectors.groupingBy(foto -> foto.getEtapa().getIdEtapaObra()));

    // 4. Transformamos las etapas y les anidamos sus fotos correspondientes
    return etapas.stream()
      .sorted(Comparator.comparingInt(e -> e.getEtapa().getNumero()))
      .map(etapa -> {
        // Dejamos que tu mapper convierta la información base de la etapa
        EtapaObraDTO dto = mapper.toDTO(etapa);

        // Sacamos las fotos de ESTA etapa específica desde nuestro Map en memoria
        List<FotoEtapa> fotosDeEstaEtapa = fotosPorEtapa.getOrDefault(etapa.getIdEtapaObra(), new ArrayList<>());

        // Si hay fotos, las mapeamos y las anidamos al DTO
        if (!fotosDeEstaEtapa.isEmpty()) {
          List<FotoEtapaDTO> fotosDTO = fotosDeEstaEtapa.stream().map(foto -> {
            FotoEtapaDTO fDto = new FotoEtapaDTO();
            fDto.setIdFoto(foto.getIdFoto());
            fDto.setIdEtapa(etapa.getIdEtapaObra());
            fDto.setRutaArchivo(foto.getRutaArchivo());
            fDto.setNombreArchivo(foto.getNombreArchivo());
            fDto.setFechaSubida(foto.getFechaSubida());
            fDto.setIdUsuarioSubida(foto.getSubidaPor() != null ? foto.getSubidaPor().getId() : null);
            fDto.setNombreUsuarioSubida(foto.getSubidaPor() != null ? foto.getSubidaPor().getUsername() : null);
            return fDto;
          }).collect(Collectors.toList());

          dto.setFotos(fotosDTO);
        }

        return dto;
      })
      .collect(Collectors.toList());
  }

  //Metodo para enviar notificaciones
  private List<String> obtenerTokensOng(Solicitud solicitud) {
    String cuit = solicitud.getCuitOrg().getCuit();
    List<Integrante> integrantes = integranteRepo.findByOrganizacion_Cuit(cuit);

    return integrantes.stream()
      .filter(i -> Boolean.TRUE.equals(i.getActivo()))
      .map(Integrante::getUsuario)
      .filter(u -> u != null && u.getPushToken() != null && !u.getPushToken().isBlank())
      .map(Usuario::getPushToken)
      .toList();
  }
}