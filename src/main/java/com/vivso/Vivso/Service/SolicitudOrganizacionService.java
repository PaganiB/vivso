package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.*;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.*;
import com.vivso.Vivso.Repository.IIntegranteRepository;
import com.vivso.Vivso.Repository.IOrganizacionRepository;
import com.vivso.Vivso.Repository.ISolicitudOrganizacionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
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

    @Override
    @Transactional
    public void registrarOrganizacion(RegistroOrganizacionDTO dto, MultiValueMap<String, MultipartFile> mapaArchivos) {
        String cuit = dto.getOrganizacion().getCuit();

        if (orgRepo.existsById(cuit)) throw new RuntimeException("El CUIT ya es una organización activa.");
        if (solicitudOrgRepo.existsByCuit(cuit)) throw new RuntimeException("Ya hay una solicitud en trámite para este CUIT.");

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

        if (mapaArchivos != null) {
            mapaArchivos.forEach((clave, listaArchivos) -> {
                if (!clave.equals("datos") && !listaArchivos.isEmpty()) {
                    String rutaFisica = documentoService.guardarArchivoFisico(listaArchivos.get(0));
                    switch (clave.toUpperCase()) {
                        case "NOTA_SOLICITUD": sol.setUrlNotaSolicitud(rutaFisica); break;
                        case "ALTA_AFIP": sol.setUrlAltaAfip(rutaFisica); break;
                        case "ACTA_COMPROMISO_ONG": sol.setUrlActaCompromiso(rutaFisica); break;
                        case "ACTA_ASAMBLEA": sol.setUrlActaAsamblea(rutaFisica); break;
                        case "CONSTANCIA_VIGENCIA": sol.setUrlConstanciaVigencia(rutaFisica); break;
                        case "CONSTANCIA_CUENTA_BANCARIA": sol.setUrlConstanciaCuentaBancaria(rutaFisica); break;
                        case "DNI_PRESIDENTE": sol.setUrlDniPresidente(rutaFisica); break;
                        case "CERTIFICADO_RESIDENCIA_PRESIDENTE": sol.setUrlCertificadoResidenciaPresidente(rutaFisica); break;
                        case "DNI_TESORERO": sol.setUrlDniTesorero(rutaFisica); break;
                        case "CERTIFICADO_RESIDENCIA_TESORERO": sol.setUrlCertificadoResidenciaTesorero(rutaFisica); break;
                    }
                }
            });
        }

        solicitudOrgRepo.save(sol);
    }

    @Override
    @Transactional
    public void aprobarOrganizacion(String cuit) {
        SolicitudOrganizacion sol = solicitudOrgRepo.findByCuit(cuit)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada para el CUIT: " + cuit));

        if (sol.getEstado() == EstadoSolicitud.Aprobada) throw new RuntimeException("Ya está aprobada.");
        if (orgRepo.existsById(cuit)) throw new RuntimeException("Ya existe una organización activa con el CUIT: " + cuit);

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

        crearDocumentoSiExiste(sol.getUrlNotaSolicitud(), TipoDocumento.NOTA_SOLICITUD, cuit);
        crearDocumentoSiExiste(sol.getUrlAltaAfip(), TipoDocumento.ALTA_AFIP, cuit);
        crearDocumentoSiExiste(sol.getUrlActaAsamblea(), TipoDocumento.ACTA_ASAMBLEA, cuit);
        crearDocumentoSiExiste(sol.getUrlActaCompromiso(), TipoDocumento.ACTA_COMPROMISO_ONG, cuit);
        crearDocumentoSiExiste(sol.getUrlConstanciaVigencia(), TipoDocumento.CONSTANCIA_VIGENCIA, cuit);
        crearDocumentoSiExiste(sol.getUrlConstanciaCuentaBancaria(), TipoDocumento.CONSTANCIA_CUENTA_BANCARIA, cuit);
        crearDocumentoSiExiste(sol.getUrlDniPresidente(), TipoDocumento.DNI_PRESIDENTE, cuit);
        crearDocumentoSiExiste(sol.getUrlCertificadoResidenciaPresidente(), TipoDocumento.CERTIFICADO_RESIDENCIA_PRESIDENTE, cuit);
        crearDocumentoSiExiste(sol.getUrlDniTesorero(), TipoDocumento.DNI_TESORERO, cuit);
        crearDocumentoSiExiste(sol.getUrlCertificadoResidenciaTesorero(), TipoDocumento.CERTIFICADO_RESIDENCIA_TESORERO, cuit);

        sol.setEstado(EstadoSolicitud.Aprobada);
        sol.setFechaAprobacion(LocalDate.now());
        sol.setMotivo("Aprobada por el Ministerio. Pasada a tablas oficiales.");
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
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada."));

        if (sol.getEstado() == EstadoSolicitud.Aprobada) throw new RuntimeException("No se puede rechazar una ONG ya aprobada.");

        documentoService.borrarArchivoFisico(sol.getUrlNotaSolicitud());
        documentoService.borrarArchivoFisico(sol.getUrlAltaAfip());
        documentoService.borrarArchivoFisico(sol.getUrlActaAsamblea());
        documentoService.borrarArchivoFisico(sol.getUrlActaCompromiso());
        documentoService.borrarArchivoFisico(sol.getUrlConstanciaVigencia());
        documentoService.borrarArchivoFisico(sol.getUrlConstanciaCuentaBancaria());
        documentoService.borrarArchivoFisico(sol.getUrlDniPresidente());
        documentoService.borrarArchivoFisico(sol.getUrlCertificadoResidenciaPresidente());
        documentoService.borrarArchivoFisico(sol.getUrlDniTesorero());
        documentoService.borrarArchivoFisico(sol.getUrlCertificadoResidenciaTesorero());

        sol.setEstado(EstadoSolicitud.Rechazada);
        sol.setMotivo(motivo);
        sol.setFechaRechazo(LocalDate.now());
        solicitudOrgRepo.save(sol);

        if (sol.getCorreoPresidente() != null && sol.getCorreoPresidente().contains("@")) {
            emailService.enviarNotificacionSolicitudRechazada(sol.getCorreoPresidente(), sol.getNombre(), motivo);
        }
    }

    @Override
    @Transactional
    public void marcarObservaciones(Integer idSolicitud, List<String> camposObservados, String motivo) {
        SolicitudOrganizacion sol = solicitudOrgRepo.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        sol.setEstado(EstadoSolicitud.SemiAprobada);
        sol.setMotivo(motivo);
        sol.setCamposObservados(String.join(",", camposObservados));
        solicitudOrgRepo.save(sol);

        // Enviar email al presidente (y tesorero si tiene)
        emailService.enviarNotificacionObservaciones(
                sol.getCorreoPresidente(),
                sol.getNombre(),
                motivo,
                camposObservados,
                sol.getTokenEdicion()
        );

        if (sol.getCorreoTesorero() != null) {
            emailService.enviarNotificacionObservaciones(
                    sol.getCorreoTesorero(),
                    sol.getNombre(),
                    motivo,
                    camposObservados,
                    sol.getTokenEdicion()
            );
        }

        log.info("Solicitud {} marcada con observaciones", idSolicitud);
    }

    @Override
    public SolicitudOrganizacionDTO obtenerSolicitudOrgPorToken(String token) {
        SolicitudOrganizacion sol = solicitudOrgRepo.findByTokenEdicion(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (sol.getEstado() != EstadoSolicitud.SemiAprobada) {
            throw new RuntimeException("Esta solicitud no tiene observaciones pendientes");
        }

        return mapper.toDTO(sol);
    }

    @Override
    @Transactional
    public void actualizarSolicitudOrgConToken(String token, SolicitudOrganizacionPatchDTO dto) {
        SolicitudOrganizacion sol = solicitudOrgRepo.findByTokenEdicion(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (sol.getEstado() != EstadoSolicitud.SemiAprobada) {
            throw new RuntimeException("Esta solicitud no está en observaciones");
        }

        mapper.updateFromPatchDto(dto, sol);

        // Vuelve a Pendiente para que el operador la revise de nuevo
        sol.setEstado(EstadoSolicitud.Pendiente);
        sol.setCamposObservados(null);
        sol.setMotivo(null);

        solicitudOrgRepo.save(sol);

        log.info("Solicitud {} actualizada vía token, vuelve a estado Pendiente", sol.getIdSolicitudOrg());
    }

    private void crearDocumentoSiExiste(String url, TipoDocumento tipo, String cuitOrg) {
        if (url != null && !url.isEmpty()) {
            documentoService.crearDesdeRutaExistente(url, tipo, null, cuitOrg);
        }
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
}