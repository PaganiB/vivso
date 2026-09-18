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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    @Override
    @Transactional
    public void registrarOrganizacion(RegistroOrganizacionDTO dto, MultiValueMap<String, MultipartFile> mapaArchivos) {
        String cuit = dto.getOrganizacion().getCuit();

        if (orgRepo.existsById(cuit)) throw new ReglaDeNegocioException("El CUIT ya es una organización activa.", HttpStatus.CONFLICT);
        if (solicitudOrgRepo.existsByCuitAndEstadoIn(cuit, List.of(EstadoSolicitud.Pendiente, EstadoSolicitud.SemiAprobada)))
            throw new ReglaDeNegocioException("Ya hay una solicitud en trámite para este CUIT.", HttpStatus.CONFLICT);

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

        sol = solicitudOrgRepo.save(sol);

        // 2° RECIÉN AHORA se procesan los archivos, ya con idSolicitudOrg disponible
        if (mapaArchivos != null) {
            Integer idSol = sol.getIdSolicitudOrg();
            mapaArchivos.forEach((clave, listaArchivos) -> {
                if (!clave.equals("datos") && !listaArchivos.isEmpty()) {
                    try {
                        TipoDocumento tipoDoc = TipoDocumento.valueOf(clave.toUpperCase());
                        documentoService.subirDocumentoDeSolicitudOrg(listaArchivos.get(0), tipoDoc, idSol);
                    } catch (IllegalArgumentException e) {
                        log.warn("Clave de documento ignorada: {}", clave);
                    }
                }
            });
        }
    }

    @Override
    @Transactional
    public void aprobarOrganizacion(String cuit) {
        SolicitudOrganizacion sol = solicitudOrgRepo.findByCuit(cuit)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada para el CUIT: " + cuit));

        if (sol.getEstado() == EstadoSolicitud.Aprobada) throw new ReglaDeNegocioException("Ya está aprobada.", HttpStatus.CONFLICT);
        if (orgRepo.existsById(cuit)) throw new ReglaDeNegocioException("Ya existe una organización activa con el CUIT: " + cuit, HttpStatus.CONFLICT);

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

        if (sol.getDniTesorero() != null) {
            Integrante tesorero = Integrante.builder()
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

        generarUsuarioYEnviarMail(sol.getNombrePresidente(), sol.getApellidoPresidente(), sol.getCorreoPresidente(), org.getNombre());

        if (sol.getDniTesorero() != null) {
            generarUsuarioYEnviarMail(sol.getNombreTesorero(), sol.getApellidoTesorero(), sol.getCorreoTesorero(), org.getNombre());
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
    private void generarUsuarioYEnviarMail(String nombre, String apellido, String correo, String nombreOrg) {
        // Validamos que tenga un correo válido
        if (correo != null && correo.contains("@")) {

            // 1. Fabricamos el nombre de usuario (Ej: maria.gomez)
            String usernameBase = nombre.toLowerCase() + "." + apellido.toLowerCase();
            String username = usernameBase.replaceAll("[^a-z0-9.]", "");
            int contador = 1;

            // Verificamos que no exista en la BD
            while (usuarioService.existePorUsername(username)) {
                username = usernameBase + contador++;
            }

            // 2. Fabricamos la contraseña temporal
            String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
            StringBuilder password = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < 12; i++) {
                password.append(caracteres.charAt(random.nextInt(caracteres.length())));
            }

            // 3. Guardamos el nuevo usuario
            UsuarioRegistroDTO nuevoUsuario = UsuarioRegistroDTO.builder()
                    .username(username)
                    .email(correo)
                    .password(password.toString())
                    .rol("INTEGRANTE") // Ambos tendrán el mismo rol en el sistema
                    .build();

            usuarioService.registrarNuevoUsuario(nuevoUsuario);

            // 4. Disparamos el correo usando el EmailService de Marian
            emailService.enviarCredencialesYAprobacion(
                    correo,
                    username,
                    password.toString(),
                    nombreOrg
            );
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
}