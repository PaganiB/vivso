package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.EnviarEtapaDTO;
import com.vivso.Vivso.DTO.EtapaObraDTO;
import com.vivso.Vivso.DTO.ObservarEtapaDTO;
import com.vivso.Vivso.DTO.ProrrogaEtapaDTO;
import com.vivso.Vivso.Exception.ReglaDeNegocioException;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.EstadoSolicitud;
import com.vivso.Vivso.Model.EtapaObra;
import com.vivso.Vivso.Model.EstadoEtapa;
import com.vivso.Vivso.Model.EtapaConstruccion;
import com.vivso.Vivso.Model.Solicitud;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Repository.IEtapaObraRepository;
import com.vivso.Vivso.Repository.IIntegranteRepository;
import com.vivso.Vivso.Repository.ISolicitudRepository;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class EtapaObraService implements IEtapaObraService {

    // El servidor puede estar en UTC: sin esto, "hoy" cambiaría a las 21 hs de Argentina
    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final int TOPE_PRORROGA_DIAS = 14;

    @Autowired private IEtapaObraRepository etapaRepo;
    @Autowired private ISolicitudRepository solicitudRepo;
    @Autowired private IUsuarioRepository usuarioRepo;
    @Autowired private IIntegranteRepository integranteRepo;
    @Autowired private CalendarioService calendario;
    @Autowired private VivsoMapper mapper;

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
        return aDTOs(etapas);
    }

    // ======================== LISTAR ========================

    @Transactional(readOnly = true)
    public List<EtapaObraDTO> listarEtapas(Integer idSolicitud, String email, boolean esAdmin) {
        Solicitud solicitud = buscarSolicitud(idSolicitud);
        if (!esAdmin && !esTecnicoAsignado(solicitud, email) && !esIntegranteDeLaOrganizacion(solicitud, email)) {
            throw prohibido("No tenés permiso para ver las etapas de este expediente.");
        }
        return aDTOs(etapaRepo.findBySolicitud_IdSolicitud(idSolicitud));
    }

    // ======================== ENVIAR (integrante de la ONG) ========================

    @Transactional
    public EtapaObraDTO enviarEtapa(Integer idEtapa, EnviarEtapaDTO dto, String email) {
        EtapaObra etapa = buscarEtapa(idEtapa);
        if (!esIntegranteDeLaOrganizacion(etapa.getSolicitud(), email)) {
            throw prohibido("Solo un integrante de la organización del expediente puede enviar la etapa.");
        }
        if (etapa.getEstado() != EstadoEtapa.EN_CURSO && etapa.getEstado() != EstadoEtapa.OBSERVADA) {
            throw conflicto("Solo se puede enviar una etapa en curso u observada. Estado actual: " + etapa.getEstado());
        }
        // TODO: exigir al menos una foto cargada cuando exista FotoEtapa

        etapa.setEstado(EstadoEtapa.EN_REVISION);
        etapa.setFechaEnvio(LocalDateTime.now(ZONA).truncatedTo(ChronoUnit.MINUTES));
        etapa.setEnviadoPor(buscarUsuario(email));
        etapa.setObservacionOrganizacion(dto != null ? dto.getObservacionOrganizacion() : null);
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
        etapa.setFechaRevision(LocalDateTime.now(ZONA).truncatedTo(ChronoUnit.MINUTES));

        // Habilita la siguiente: desde este momento corre su plazo
        EtapaConstruccion siguiente = etapa.getEtapa().obtenerSiguienteEtapa();
        if (siguiente != null) {
            EtapaObra proxima = etapaRepo
                    .findBySolicitud_IdSolicitudAndEtapa(etapa.getSolicitud().getIdSolicitud(), siguiente)
                    .orElseThrow(() -> noEncontrado("No se encontró la etapa siguiente del expediente."));
            habilitar(proxima);
            etapaRepo.save(proxima);
        }
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
        etapa.setFechaRevision(LocalDateTime.now(ZONA).truncatedTo(ChronoUnit.MINUTES));
        return mapper.toDTO(etapaRepo.save(etapa));
    }

    // ======================== PRÓRROGA (técnico asignado) ========================

    @Transactional
    public EtapaObraDTO prorrogarEtapa(Integer idEtapa, ProrrogaEtapaDTO dto, String email) {
        EtapaObra etapa = buscarEtapa(idEtapa);
        validarTecnicoAsignado(etapa.getSolicitud(), email);

        if (etapa.getEstado() != EstadoEtapa.EN_CURSO &&
                etapa.getEstado() != EstadoEtapa.OBSERVADA &&
                etapa.getEstado() != EstadoEtapa.PRORROGADA) {
            throw conflicto("Solo se puede prorrogar una etapa en curso, observada o en revisión. Estado actual: " + etapa.getEstado());
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
        return etapas.stream()
                .sorted(Comparator.comparingInt(e -> e.getEtapa().getNumero()))
                .map(mapper::toDTO)
                .toList();
    }
}