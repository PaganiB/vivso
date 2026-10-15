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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SolicitudService implements ISolicitudService {

    @Autowired private ISolicitudRepository solicitudRepo;
    @Autowired private IOrganizacionRepository orgRepo;
    @Autowired private IViviendaRepository viviendaRepo;
    @Autowired private IFamiliaRepository familiaRepo;
    @Autowired private IFamiliarRepository familiarRepo;
    @Autowired private IDocumentoService documentoService;
    @Autowired private IVisitaInicialRepository visitaInicialRepository;
    @Autowired private VivsoMapper mapper;
    @Autowired private IIntegranteRepository integranteRepo;
    @Autowired private IUsuarioRepository usuarioRepo;
    @Autowired private IEtapaObraRepository etapaObraRepo;

    @Override
    @Transactional
    public SolicitudDTO registrarSolicitudFamilia(RegistroFamiliaDTO dto,
                                                  MultiValueMap<String, MultipartFile> mapaArchivos) {

        Organizacion org = orgRepo.findById(dto.getCuitOrg())
                .orElseThrow(() -> new RecursoNoEncontradoException("Organización no encontrada: " + dto.getCuitOrg()));

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

        Integer idSeguro = tecnico.getId();

        // 2. Buscamos las solicitudes asignadas al técnico
        List<Solicitud> solicitudes = solicitudRepo.buscarPorTecnicoAsignado(idSeguro);

        // 3. Filtramos: que estén Pendientes o Aprobadas, Y QUE NO TENGAN Visita Inicial registrada
        return solicitudes.stream()
                .filter(s -> s.getEstado() == EstadoSolicitud.Pendiente || s.getEstado() == EstadoSolicitud.Aprobada)
                .filter(s -> {
                    // Verificamos si ya existe una visita inicial para esta solicitud
                    List<VisitaInicial> visitas = visitaInicialRepository.findBySolicitud_IdSolicitudOrderByFechaVisitaDesc(s.getIdSolicitud());
                    // Si la lista de visitas está vacía, significa que todavía está pendiente de relevamiento
                    return visitas.isEmpty();
                })
                .map(solicitud -> {
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

        Integer idSeguro = tecnico.getId();

        List<Solicitud> solicitudes = solicitudRepo.buscarPorTecnicoAsignado(idSeguro);

        return solicitudes.stream().map(s -> {
            ExpedienteWebDTO dto = new ExpedienteWebDTO();
            dto.setIdSolicitud(s.getIdSolicitud());
            dto.setNumExp(s.getNumExp());
            dto.setEstadoSolicitud(s.getEstado());

            if (s.getFamiliaBeneficiaria() != null) {
                dto.setBeneficiario(s.getFamiliaBeneficiaria().getNombreRepresentante());
                dto.setDni(s.getFamiliaBeneficiaria().getDniRepresentante() != null ? s.getFamiliaBeneficiaria().getDniRepresentante() : "S/D");
            } else {
                dto.setBeneficiario("Sin asignar");
                dto.setDni("S/D");
            }

            // 1. FASE DE OBRA
            if (s.getEstado() == EstadoSolicitud.Aprobada) {
                Optional<Vivienda> viviendaOpt = viviendaRepo.findBySolicitud_IdSolicitud(s.getIdSolicitud());

                if (viviendaOpt.isPresent()) {
                    Vivienda v = viviendaOpt.get();
                    dto.setEstadoRelevamiento("En Construcción");
                    dto.setUltimoRegistro(v.getFechaInic() != null ? v.getFechaInic().toString() : "Fase de obra");

                    // 👇 LÓGICA DE ETAPA ACTUAL CON ÚLTIMO REGISTRO
                    List<EtapaObra> etapas = etapaObraRepo.findBySolicitud_IdSolicitud(s.getIdSolicitud());

                    if (etapas != null && !etapas.isEmpty()) {
                        // Buscamos cuál es la etapa activa
                        Optional<EtapaObra> etapaActiva = etapas.stream()
                                .filter(e -> e.getEstado() == EstadoEtapa.EN_CURSO ||
                                        e.getEstado() == EstadoEtapa.OBSERVADA ||
                                        e.getEstado() == EstadoEtapa.PRORROGADA ||
                                        e.getEstado() == EstadoEtapa.EN_REVISION)
                                .findFirst();

                        if (etapaActiva.isPresent()) {
                            EtapaObra e = etapaActiva.get();

                            int numero = e.getEtapa().getNumero();
                            String nombre = e.getEtapa().getNombre();

                            dto.setEtapaActual("Etapa " + numero + ": " + nombre + " (" + e.getEstado() + ")");

                            // 👇 ASIGNAR LA FECHA DE ENVÍO DE LA ONG (ultimoRegistro)
                            if (e.getFechaEnvio() != null) {
                                // Formateamos el LocalDateTime a algo legible: "dd/MM/yyyy HH:mm"
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                                dto.setUltimoRegistro(e.getFechaEnvio().format(formatter));
                            } else {
                                // Si la etapa está EN_CURSO pero la ONG todavía no mandó nada a revisión
                                dto.setUltimoRegistro("Sin envíos a revisión");
                            }

                        } else {
                            // Si no hay ninguna activa, verificamos si ya terminaron todas
                            boolean todasAprobadas = etapas.stream().allMatch(e -> e.getEstado() == EstadoEtapa.APROBADA);
                            if (todasAprobadas) {
                                dto.setEtapaActual("Obra Finalizada");
                                dto.setUltimoRegistro("-");
                            } else {
                                dto.setEtapaActual("Esperando habilitación de etapa");
                                dto.setUltimoRegistro("-");
                            }
                        }
                    } else {
                        dto.setEtapaActual("Sin etapas generadas");
                        dto.setUltimoRegistro("-");
                    }
                } else {
                    dto.setEstadoRelevamiento("Aprobada - Esperando inicio");
                    dto.setUltimoRegistro("-");
                    dto.setEtapaActual("Sin iniciar");
                }
            }
            // 2. FASE INICIAL
            else {
                List<VisitaInicial> historialVisitas = visitaInicialRepository.findBySolicitud_IdSolicitudOrderByFechaVisitaDesc(s.getIdSolicitud());

                if (!historialVisitas.isEmpty()) {
                    dto.setEstadoRelevamiento("Relevamiento en proceso");
                    dto.setUltimoRegistro(historialVisitas.get(0).getFechaVisita().toLocalDate().toString());
                } else {
                    dto.setEstadoRelevamiento("Sin relevamiento");
                    dto.setUltimoRegistro("Sin relevamiento");
                }

                dto.setEtapaActual("Fase Previa");
            }

            return dto;
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

        // 3. Buscar solo los expedientes en fase de obra para esa ONG
        List<Solicitud> obrasAsignadas = solicitudRepo.buscarObrasParaOng(cuitOng);

        // 4. Mapear a DTO para la app móvil aplicando el nuevo filtro
        return obrasAsignadas.stream()
                // 👇 NUEVA REGLA: Excluimos las obras que todavía no tienen etapas iniciadas
                .filter(solicitud -> {
                    List<EtapaObra> etapas = etapaObraRepo.findBySolicitud_IdSolicitud(solicitud.getIdSolicitud());
                    return etapas != null && !etapas.isEmpty();
                })
                .map(solicitud -> {
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