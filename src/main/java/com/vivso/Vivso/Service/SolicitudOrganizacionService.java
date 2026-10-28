package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.*;
import com.vivso.Vivso.Exception.RecursoNoEncontradoException;
import com.vivso.Vivso.Exception.ReglaDeNegocioException;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.*;
import com.vivso.Vivso.Repository.IIntegranteRepository;
import com.vivso.Vivso.Repository.IOrganizacionRepository;
import com.vivso.Vivso.Repository.ISolicitudOrganizacionRepository;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class SolicitudOrganizacionService implements ISolicitudOrganizacionService {

    @Autowired private ISolicitudOrganizacionRepository solicitudOrgRepo;
    @Autowired private IOrganizacionRepository orgRepo;
    @Autowired private IIntegranteRepository integranteRepo;
    @Autowired private VivsoMapper mapper;
    @Autowired private IUsuarioService usuarioService;
    @Autowired private IEmailService emailService;
    @Autowired private IDocumentoService documentoService;
    @Autowired private IUsuarioRepository usuarioRepo;
    @Autowired private PasswordEncoder passwordEncoder;

  @Override
  // 1. ELIMINAMOS el @Transactional de aquí arriba.
  // Esto es vital: la conexión a la base de datos ya no estará secuestrada durante la subida.
  public void registrarOrganizacion(RegistroOrganizacionDTO dto, MultiValueMap<String, MultipartFile> mapaArchivos) {
    String cuit = dto.getOrganizacion().getCuit();

    // Validaciones iniciales
    if (orgRepo.existsById(cuit))
      throw new ReglaDeNegocioException("El CUIT ya es una organización activa.", HttpStatus.CONFLICT);
    if (solicitudOrgRepo.existsByCuitAndEstadoIn(cuit, List.of(EstadoSolicitud.Pendiente, EstadoSolicitud.SemiAprobada)))
      throw new ReglaDeNegocioException("Ya hay una solicitud en trámite para este CUIT.", HttpStatus.CONFLICT);

    // 2. Guardado Rápido (Milisegundos)
    // Spring aplica un @Transactional automático y muy breve solo para este .save()
    SolicitudOrganizacion solNueva = construirEntidad(dto, cuit);
    final SolicitudOrganizacion solGuardada = solicitudOrgRepo.save(solNueva);
    final Integer idSol = solGuardada.getIdSolicitudOrg();

    // 3. Procesamiento Paralelo de Archivos
    if (mapaArchivos != null) {
      List<CompletableFuture<Void>> hilosDeSubida = new ArrayList<>();

      mapaArchivos.forEach((clave, listaArchivos) -> {
        if (!clave.equals("datos") && !listaArchivos.isEmpty()) {
          MultipartFile archivo = listaArchivos.get(0);

          // Disparamos un hilo secundario por cada archivo
          CompletableFuture<Void> tarea = CompletableFuture.runAsync(() -> {
            try {
              TipoDocumento tipoDoc = TipoDocumento.valueOf(clave.toUpperCase());
              documentoService.subirDocumentoDeSolicitudOrg(archivo, tipoDoc, idSol);
            } catch (IllegalArgumentException e) {
              log.warn("Clave de documento ignorada: {}", clave);
            } catch (Exception e) {
              log.error("Fallo crítico subiendo el documento {} para la solicitud {}: {}", clave, idSol, e.getMessage());
              throw new RuntimeException("Error en subida", e); // Rompe el hilo si Cloudinary falla
            }
          });

          hilosDeSubida.add(tarea);
        }
      });

      // 4. Barrera de Sincronización (Join)
      try {
        // Esperamos que TODOS los archivos terminen de subir al mismo tiempo
        CompletableFuture.allOf(hilosDeSubida.toArray(new CompletableFuture[0])).join();
      } catch (Exception e) {
        // Si falla el internet o Cloudinary, le avisamos al frontend
        throw new ReglaDeNegocioException("La organización se registró, pero hubo un error subiendo algunos documentos. Por favor, revise su conexión.", HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
  }

    @Override
    @Transactional
    public void aprobarOrganizacion(String cuit) {
        SolicitudOrganizacion sol = solicitudOrgRepo.findByCuit(cuit)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada para el CUIT: " + cuit));

        if (sol.getEstado() == EstadoSolicitud.Aprobada) throw new ReglaDeNegocioException("Ya está aprobada.", HttpStatus.CONFLICT);
        if (orgRepo.existsById(cuit)) throw new ReglaDeNegocioException("Ya existe una organización activa con el CUIT: " + cuit, HttpStatus.CONFLICT);

        validarCorreosDisponibles(sol);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario revisor = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario autenticado no encontrado"));

        Organizacion org = Organizacion.builder()
                .cuit(sol.getCuit()).nombre(sol.getNombre()).tipo(sol.getTipo())
                .dom_legal(sol.getDomLegal()).cpe(sol.getCpe())
                .fechaVencimientoVigencia(sol.getFechaVencimientoVigencia())
                .fechaUltimaAsamblea(sol.getFechaUltimaAsamblea())
                .build();
        orgRepo.save(org);

        Integrante presidente = Integrante.builder()
                .dni(sol.getDniPresidente()).nombre(sol.getNombrePresidente())
                .apellido(sol.getApellidoPresidente()).correo(sol.getCorreoPresidente())
                .telefono(sol.getTelefonoPresidente()).domicilio(sol.getDomicilioPresidente())
                .fechaAltaCargo(sol.getFechaAltaCargoPresidente())
                .cargo(TipoCargo.PRESIDENTE).activo(true).organizacion(org)
                .build();
        integranteRepo.save(presidente);

        Integrante tesorero = null;
        if (sol.getDniTesorero() != null) {
            tesorero = Integrante.builder()
                    .dni(sol.getDniTesorero()).nombre(sol.getNombreTesorero())
                    .apellido(sol.getApellidoTesorero()).correo(sol.getCorreoTesorero())
                    .telefono(sol.getTelefonoTesorero()).domicilio(sol.getDomicilioTesorero())
                    .fechaAltaCargo(sol.getFechaAltaCargoTesorero())
                    .cargo(TipoCargo.TESORERO).activo(true).organizacion(org)
                    .build();
            integranteRepo.save(tesorero);
        }

        List<DocumentoDTO> docs = documentoService.listarPorSolicitudOrg(sol.getIdSolicitudOrg());
        for (DocumentoDTO d : docs) {
            documentoService.aprobarConOrganizacion(d.getIdDocumento(), org, revisor);
        }

        sol.setEstado(EstadoSolicitud.Aprobada);
        sol.setFechaAprobacion(LocalDate.now());
        sol.setRevisor(revisor);
        solicitudOrgRepo.save(sol);

        Usuario usuarioPresidente = generarUsuarioYEnviarMail(sol.getNombrePresidente(), sol.getApellidoPresidente(), sol.getCorreoPresidente(), org.getNombre());
        if (usuarioPresidente != null) {
            presidente.setUsuario(usuarioPresidente);
            integranteRepo.save(presidente);
        }

        if (sol.getDniTesorero() != null) {
            Usuario usuarioTesorero = generarUsuarioYEnviarMail(sol.getNombreTesorero(), sol.getApellidoTesorero(), sol.getCorreoTesorero(), org.getNombre());
            if (usuarioTesorero != null) {
                tesorero.setUsuario(usuarioTesorero);
                integranteRepo.save(tesorero);
            }
        }
    }

    @Override
    @Transactional
    public void rechazarOrganizacion(String cuit, String motivo) {
        SolicitudOrganizacion sol = solicitudOrgRepo.findByCuit(cuit)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada."));

        if (sol.getEstado() == EstadoSolicitud.Aprobada)
            throw new ReglaDeNegocioException("No se puede rechazar una ONG ya aprobada.", HttpStatus.CONFLICT);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario revisor = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario autenticado no encontrado"));

        List<DocumentoDTO> docs = documentoService.listarPorSolicitudOrg(sol.getIdSolicitudOrg());
        for (DocumentoDTO d : docs) {
            documentoService.rechazar(d.getIdDocumento(), revisor);
        }

        sol.setEstado(EstadoSolicitud.Rechazada);
        sol.setMotivo(motivo);
        sol.setFechaRechazo(LocalDate.now());
        sol.setRevisor(revisor);
        solicitudOrgRepo.save(sol);

        if (sol.getCorreoPresidente() != null && sol.getCorreoPresidente().contains("@")) {
            emailService.enviarNotificacionSolicitudRechazada(sol.getCorreoPresidente(), sol.getNombre(), motivo);
        }
    }

    @Override
    @Transactional
    public void marcarObservaciones(Integer idSolicitud, List<String> camposObservados, String motivo) {
        SolicitudOrganizacion sol = solicitudOrgRepo.findById(idSolicitud)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario revisor = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario autenticado no encontrado"));

        // Buscamos los documentos que YA fueron marcados A_CORREGIR (con marcarParaCorregir)
        List<DocumentoDTO> documentosACorregir = documentoService.listarPorSolicitudOrg(sol.getIdSolicitudOrg())
                .stream()
                .filter(d -> d.getEstado() == EstadoDocumento.A_CORREGIR)
                .toList();

        // Armamos una lista de nombres/tipos legibles para el mail
        List<String> nombresDocumentos = documentosACorregir.stream()
                .map(d -> d.getTipo().getValor()) // o d.getTipo().name(), según lo que quieras mostrar
                .toList();

        sol.setEstado(EstadoSolicitud.SemiAprobada);
        sol.setMotivo(motivo);
        sol.setCamposObservados(String.join(",", camposObservados));
        sol.setRevisor(revisor);
        solicitudOrgRepo.save(sol);

        // Combinamos campos de texto + documentos en una sola lista para el mail
        List<String> todoLoObservado = new ArrayList<>(camposObservados);
        todoLoObservado.addAll(nombresDocumentos);

        emailService.enviarNotificacionObservaciones(
                sol.getCorreoPresidente(), sol.getNombre(), motivo, todoLoObservado, sol.getTokenEdicion());

        if (sol.getCorreoTesorero() != null) {
            emailService.enviarNotificacionObservaciones(
                    sol.getCorreoTesorero(), sol.getNombre(), motivo, todoLoObservado, sol.getTokenEdicion());
        }

        log.info("Solicitud {} marcada con observaciones", idSolicitud);
    }

    @Override
    public SolicitudOrganizacionDTO obtenerSolicitudOrgPorToken(String token) {
        SolicitudOrganizacion sol = solicitudOrgRepo.findByTokenEdicion(token)
                .orElseThrow(() -> new RecursoNoEncontradoException("Token inválido"));

        if (sol.getEstado() != EstadoSolicitud.SemiAprobada) {
            throw new ReglaDeNegocioException("Esta solicitud no tiene observaciones pendientes", HttpStatus.CONFLICT);
        }

        SolicitudOrganizacionDTO dto = mapper.toDTO(sol);
        dto.setDocumentos(documentoService.listarPorSolicitudOrg(sol.getIdSolicitudOrg()));
        return dto;
    }

    @Override
    @Transactional
    public void actualizarSolicitudOrgConToken(String token, SolicitudOrganizacionPatchDTO dto) {
        SolicitudOrganizacion sol = solicitudOrgRepo.findByTokenEdicion(token)
                .orElseThrow(() -> new RecursoNoEncontradoException("Token inválido"));

        if (sol.getEstado() != EstadoSolicitud.SemiAprobada) {
            throw new ReglaDeNegocioException("Esta solicitud no está en observaciones", HttpStatus.CONFLICT);
        }

        boolean quedanDocumentosSinCorregir = documentoService.listarPorSolicitudOrg(sol.getIdSolicitudOrg())
                .stream()
                .anyMatch(d -> d.getEstado() == EstadoDocumento.A_CORREGIR);

        if (quedanDocumentosSinCorregir) {
            throw new ReglaDeNegocioException(
                    "Todavía hay documentos marcados para corregir sin reemplazar. Reemplazalos antes de enviar.",
                    HttpStatus.CONFLICT);
        }

        mapper.updateFromPatchDto(dto, sol);

        sol.setEstado(EstadoSolicitud.Pendiente);
        sol.setCamposObservados(null);
        sol.setMotivo(null);

        solicitudOrgRepo.save(sol);

        log.info("Solicitud {} actualizada vía token, vuelve a estado Pendiente", sol.getIdSolicitudOrg());
    }

    //METODO PARA GENERAR LAS CREDENCIALES
    private Usuario generarUsuarioYEnviarMail(String nombre, String apellido, String correo, String nombreOrg) {
        if (correo != null && correo.contains("@")) {

            // Generamos la contraseña temporal (se usa en ambos casos: reactivación o creación)
            String passwordTemporal = usuarioService.generarPasswordTemporal();

            Optional<Usuario> usuarioExistente = usuarioRepo.findByEmail(correo);

            Usuario usuarioCreado;
            if (usuarioExistente.isPresent() && !usuarioExistente.get().getActivo()) {
                Usuario u = usuarioExistente.get();
                u.setActivo(true);
                u.setDebeCambiarPassword(true);
                u.setPassword_hash(passwordEncoder.encode(passwordTemporal));
                usuarioCreado = usuarioRepo.save(u);
            } else if (usuarioExistente.isPresent()) {
                throw new ReglaDeNegocioException("El correo " + correo + " ya está en uso por un usuario activo.", HttpStatus.CONFLICT);
            } else {
                String usernameBase = nombre.toLowerCase() + "." + apellido.toLowerCase();
                String username = usernameBase.replaceAll("[^a-z0-9.]", "");
                int contador = 1;
                while (usuarioService.existePorUsername(username)) {
                    username = usernameBase + contador++;
                }

                UsuarioRegistroDTO nuevoUsuario = UsuarioRegistroDTO.builder()
                        .username(username)
                        .email(correo)
                        .password(passwordTemporal)
                        .rol("INTEGRANTE")
                        .build();

                usuarioCreado = usuarioService.registrarNuevoUsuario(nuevoUsuario);
            }

            enviarDespuesDelCommit(() ->
                    emailService.enviarCredencialesYAprobacion(correo, passwordTemporal, nombreOrg)
            );

            return usuarioCreado;
        }
        return null;
    }

    private void validarCorreosDisponibles(SolicitudOrganizacion sol) {
        String correoPresidente = sol.getCorreoPresidente();
        String correoTesorero = sol.getDniTesorero() != null ? sol.getCorreoTesorero() : null;

        // Si los dos son iguales, el segundo fallaría porque el primero ya crearía el usuario
        if (correoPresidente != null && correoPresidente.equalsIgnoreCase(correoTesorero))
            throw new ReglaDeNegocioException("El presidente y el tesorero no pueden tener el mismo correo.", HttpStatus.CONFLICT);

        for (String correo : new String[]{correoPresidente, correoTesorero}) {
            if (correo != null) {
                usuarioRepo.findByEmail(correo)
                        .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                        .ifPresent(u -> {
                            throw new ReglaDeNegocioException("El correo " + correo + " ya está en uso por un usuario activo.", HttpStatus.CONFLICT);
                        });
            }
        }
    }

    private void enviarDespuesDelCommit(Runnable envio) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        envio.run();
                    } catch (Exception e) {
                        log.error("No se pudo enviar el mail de credenciales", e);
                    }
                }
            });
        } else {
            envio.run();
        }
    }

    @Override
    @Transactional
    public void reemplazarDocumentoConToken(String token, Integer idDoc, MultipartFile archivo) {
        SolicitudOrganizacion sol = solicitudOrgRepo.findByTokenEdicion(token)
                .orElseThrow(() -> new RecursoNoEncontradoException("Token inválido"));

        if (sol.getEstado() != EstadoSolicitud.SemiAprobada) {
            throw new ReglaDeNegocioException("Esta solicitud no está en observaciones", HttpStatus.CONFLICT);
        }

        boolean perteneceASol = documentoService.listarPorSolicitudOrg(sol.getIdSolicitudOrg())
                .stream()
                .anyMatch(d -> d.getIdDocumento().equals(idDoc));

        if (!perteneceASol) {
            throw new ReglaDeNegocioException("El documento no pertenece a esta solicitud", HttpStatus.CONFLICT);
        }

        documentoService.reemplazar(idDoc, archivo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudOrganizacionDTO> listarSolicitudes() {
        return solicitudOrgRepo.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudOrganizacionDTO> listarPendientes() {
        return solicitudOrgRepo.findByEstado(EstadoSolicitud.Pendiente).stream()
                .map(mapper::toDTO)
                .toList();
    }

  // === MÉTODO DE APOYO ===
  // Lo sacamos a un método privado para que el código principal quede limpio y fácil de leer
  private SolicitudOrganizacion construirEntidad(RegistroOrganizacionDTO dto, String cuit) {
    SolicitudOrganizacion sol = SolicitudOrganizacion.builder()
      .cuit(cuit)
      .nombre(dto.getOrganizacion().getNombre())
      .tipo(dto.getOrganizacion().getTipo())
      .domLegal(dto.getOrganizacion().getDom_legal())
      .cpe(dto.getOrganizacion().getCpe())
      .fechaVencimientoVigencia(dto.getOrganizacion().getFechaVencimientoVigencia())
      .fechaUltimaAsamblea(dto.getOrganizacion().getFechaUltimaAsamblea())
      .dniPresidente(dto.getPresidente().getDni())
      .nombrePresidente(dto.getPresidente().getNombre())
      .apellidoPresidente(dto.getPresidente().getApellido())
      .correoPresidente(dto.getPresidente().getCorreo())
      .telefonoPresidente(dto.getPresidente().getTelefono())
      .fechaAltaCargoPresidente(dto.getPresidente().getFechaAltaCargo())
      .domicilioPresidente(dto.getPresidente().getDomicilio())
      .estado(EstadoSolicitud.Pendiente)
      .build();

    if (dto.getTesorero() != null) {
      sol.setDniTesorero(dto.getTesorero().getDni());
      sol.setNombreTesorero(dto.getTesorero().getNombre());
      sol.setApellidoTesorero(dto.getTesorero().getApellido());
      sol.setCorreoTesorero(dto.getTesorero().getCorreo());
      sol.setTelefonoTesorero(dto.getTesorero().getTelefono());
      sol.setFechaAltaCargoTesorero(dto.getTesorero().getFechaAltaCargo());
      sol.setDomicilioTesorero(dto.getTesorero().getDomicilio());
    }

    return sol;
  }

}

