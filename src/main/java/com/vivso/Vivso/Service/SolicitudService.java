package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.*;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.*;
import com.vivso.Vivso.Repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
public class SolicitudService implements ISolicitudService {

    @Autowired private ISolicitudRepository solicitudRepo;
    @Autowired private IOrganizacionRepository orgRepo;
    @Autowired private IIntegranteRepository integranteRepo;
    @Autowired private IViviendaRepository viviendaRepo;
    @Autowired private IFamiliaRepository familiaRepo;
    @Autowired private IFamiliarRepository familiarRepo;
    @Autowired private IDocumentoRepository docRepo;
    @Autowired private VivsoMapper mapper;
    @Autowired private IUsuarioService usuarioService;
    @Autowired private IEmailService emailService;
    @Autowired private ISolicitudOrganizacionRepository solicitudOrgRepo;

    // Constantes para los archivos físicos
    private static final String CARPETA_UPLOADS = "uploads/documentos/";
    private static final long MAX_SIZE = 20 * 1024 * 1024; // 20Mb
    private static final List<String> EXTENSIONES = List.of("pdf", "jpg", "jpeg", "png");

    @Override
    public List<SolicitudDTO> getSolicitudes() {
        return solicitudRepo.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public SolicitudDTO saveSolicitud(SolicitudDTO dto) {
        Organizacion org = orgRepo.findById(dto.getCuitOrg())
                .orElseThrow(() -> new RuntimeException("Organización no encontrada: " + dto.getCuitOrg()));

        Vivienda viv = null;
        if (dto.getEstado() == EstadoSolicitud.Aprobada) {
            if (dto.getNumExp() == null || dto.getNumExp().isBlank())
                throw new RuntimeException("Debe indicar el número de expediente para solicitudes aprobadas");
            viv = viviendaRepo.findViviendaByNumExp(dto.getNumExp())
                    .orElseThrow(() -> new RuntimeException("Vivienda no encontrada: " + dto.getNumExp()));
        }

        Familia familia = familiaRepo.findById(dto.getIdFamilia())
                .orElseThrow(() -> new RuntimeException("Familia no encontrada: " + dto.getIdFamilia()));

        Solicitud s = mapper.toEntity(dto);
        s.setCuitOrg(org);
        s.setNumExp(viv);
        s.setFamiliaBeneficiaria(familia);
        s.setFechaActivacion(dto.getEstado() == EstadoSolicitud.Aprobada ? LocalDate.now() : null);

        return mapper.toDTO(solicitudRepo.save(s));
    }

    @Override
    public SolicitudDTO updateSolicitud(Integer id, SolicitudDTO dto) {
        Solicitud s = solicitudRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + id));

        if (dto.getGDE() != null)            s.setGDE(dto.getGDE());
        if (dto.getFechaSolicitud() != null) s.setFechaSolicitud(dto.getFechaSolicitud());
        if (dto.getObservacion() != null)    s.setObservacion(dto.getObservacion());

        if (dto.getCuitOrg() != null) {
            s.setCuitOrg(orgRepo.findById(dto.getCuitOrg())
                    .orElseThrow(() -> new RuntimeException("Organización no encontrada: " + dto.getCuitOrg())));
        }

        if (dto.getIdFamilia() != null) {
            s.setFamiliaBeneficiaria(familiaRepo.findById(dto.getIdFamilia())
                    .orElseThrow(() -> new RuntimeException("Familia no encontrada: " + dto.getIdFamilia())));
        }

        if (dto.getEstado() != null) s.setEstado(dto.getEstado());

        // Lógica de estado: numExp y fechaActivacion van juntos
        EstadoSolicitud estadoFinal = s.getEstado();
        if (estadoFinal == EstadoSolicitud.Aprobada) {
            if (dto.getNumExp() == null || dto.getNumExp().isBlank())
                throw new RuntimeException("Debe indicar el número de expediente para solicitudes aprobadas");
            s.setNumExp(viviendaRepo.findViviendaByNumExp(dto.getNumExp())
                    .orElseThrow(() -> new RuntimeException("Expediente no encontrado: " + dto.getNumExp())));
            if (s.getFechaActivacion() == null) s.setFechaActivacion(LocalDate.now());
        } else {
            s.setNumExp(null);
            s.setFechaActivacion(null);
        }

        return mapper.toDTO(solicitudRepo.save(s));
    }

    @Override
    public void deleteSolicitud(Integer id) {
        Solicitud s = solicitudRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + id));
        solicitudRepo.delete(s);
    }

    @Override
    public SolicitudDTO findSolicitud(Integer id) {
        return solicitudRepo.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + id));
    }

    @Override
    public List<SolicitudDTO> buscarPorEstado(EstadoSolicitud estado) {
        List<Solicitud> result = solicitudRepo.findByEstado(estado);
        if (result.isEmpty()) throw new RuntimeException("Ninguna solicitud tiene ese estado");
        return result.stream().map(mapper::toDTO).toList();
    }

    @Override
    public List<SolicitudDTO> buscarPorOrganizacion(String cuitOrg) {
        List<Solicitud> result = solicitudRepo.findByCuitOrg_Cuit(cuitOrg);
        if (result.isEmpty()) throw new RuntimeException("No hay solicitudes para el CUIT: " + cuitOrg);
        return result.stream().map(mapper::toDTO).toList();
    }

    // =============================================
    // FORMULARIO 1: REGISTRO DE ORGANIZACIÓN
    // =============================================
    //REGISTRAR ORGANIZACION V2
    @Override
    @Transactional
    public void registrarOrganizacion(RegistroOrganizacionDTO dto, MultiValueMap<String, MultipartFile> mapaArchivos) {
        String cuit = dto.getOrganizacion().getCuit();

        // 1. Validar que no exista en tabla oficial ni en sala de espera
        if (orgRepo.existsById(cuit)) throw new RuntimeException("El CUIT ya es una organización activa.");
        if (solicitudOrgRepo.existsByCuit(cuit)) throw new RuntimeException("Ya hay una solicitud en trámite para este CUIT.");

        // 2. Armamos la fila gigante con los datos de texto
        SolicitudOrganizacion sol = SolicitudOrganizacion.builder()
                .cuit(cuit)
                .nombre(dto.getOrganizacion().getNombre())
                .tipo(dto.getOrganizacion().getTipo())
                .domLegal(dto.getOrganizacion().getDom_legal())
                .cpe(dto.getOrganizacion().getCpe())
                .fechaVencimientoVigencia(dto.getOrganizacion().getFechaVencimientoVigencia())
                .fechaUltimaAsamblea(dto.getOrganizacion().getFechaUltimaAsamblea())
                // Presidente
                .dniPresidente(dto.getPresidente().getDni())
                .nombrePresidente(dto.getPresidente().getNombre())
                .apellidoPresidente(dto.getPresidente().getApellido())
                .correoPresidente(dto.getPresidente().getCorreo())
                .telefonoPresidente(dto.getPresidente().getTelefono())
                .fechaAltaCargoPresidente(dto.getPresidente().getFechaAltaCargo())
                .domicilioPresidente(dto.getPresidente().getDomicilio())
                .estado(EstadoSolicitud.Pendiente)
                .build();

        // Si viene el Tesorero, lo sumamos
        if (dto.getTesorero() != null) {
            sol.setDniTesorero(dto.getTesorero().getDni());
            sol.setNombreTesorero(dto.getTesorero().getNombre());
            sol.setApellidoTesorero(dto.getTesorero().getApellido());
            sol.setCorreoTesorero(dto.getTesorero().getCorreo());
            sol.setTelefonoTesorero(dto.getTesorero().getTelefono());
            sol.setFechaAltaCargoTesorero(dto.getTesorero().getFechaAltaCargo());
            sol.setDomicilioTesorero(dto.getTesorero().getDomicilio());
        }

        // 3. Guardar archivos en disco y asignar las rutas a la solicitud
        if (mapaArchivos != null) {
            mapaArchivos.forEach((clave, listaArchivos) -> {
                if (!clave.equals("datos") && !listaArchivos.isEmpty()) {
                    String rutaFisica = guardarArchivoFisico(listaArchivos.get(0));
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
    // Metodos para aceptar o rechazar el form de org

    //METODO APROBAR V2
    @Override
    @Transactional
    public void aprobarOrganizacion(String cuit) {
        SolicitudOrganizacion sol = solicitudOrgRepo.findByCuit(cuit)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada para el CUIT: " + cuit));

        if (sol.getEstado() == EstadoSolicitud.Aprobada) throw new RuntimeException("Ya está aprobada.");

        // 1. Crear Organización Oficial
        Organizacion org = Organizacion.builder()
                .cuit(sol.getCuit()).nombre(sol.getNombre()).tipo(sol.getTipo())
                .dom_legal(sol.getDomLegal()).cpe(sol.getCpe())
                .fechaVencimientoVigencia(sol.getFechaVencimientoVigencia())
                .fechaUltimaAsamblea(sol.getFechaUltimaAsamblea())
                .build();
        orgRepo.save(org);

        // 2. Crear Presidente Oficial
        Integrante presidente = Integrante.builder()
                .dni(sol.getDniPresidente()).nombre(sol.getNombrePresidente())
                .apellido(sol.getApellidoPresidente()).correo(sol.getCorreoPresidente())
                .telefono(sol.getTelefonoPresidente()).domicilio(sol.getDomicilioPresidente())
                .fechaAltaCargo(sol.getFechaAltaCargoPresidente())
                .cargo(TipoCargo.PRESIDENTE).activo(true).organizacion(org)
                .build();
        integranteRepo.save(presidente);

        // 3. Crear Tesorero Oficial (Si existe)
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

        // 4. Crear Entidades Documento Oficiales (Agregamos el 'null' al final para la Familia)
        crearEntidadDocumento(sol.getUrlNotaSolicitud(), TipoDocumento.NOTA_SOLICITUD, org, null);
        crearEntidadDocumento(sol.getUrlAltaAfip(), TipoDocumento.ALTA_AFIP, org, null);
        crearEntidadDocumento(sol.getUrlActaAsamblea(), TipoDocumento.ACTA_ASAMBLEA, org, null);
        crearEntidadDocumento(sol.getUrlActaCompromiso(), TipoDocumento.ACTA_COMPROMISO_ONG, org, null);
        crearEntidadDocumento(sol.getUrlConstanciaVigencia(), TipoDocumento.CONSTANCIA_VIGENCIA, org, null);
        crearEntidadDocumento(sol.getUrlConstanciaCuentaBancaria(), TipoDocumento.CONSTANCIA_CUENTA_BANCARIA, org, null);
        crearEntidadDocumento(sol.getUrlDniPresidente(), TipoDocumento.DNI_PRESIDENTE, org, null);
        crearEntidadDocumento(sol.getUrlCertificadoResidenciaPresidente(), TipoDocumento.CERTIFICADO_RESIDENCIA_PRESIDENTE, org, null);
        crearEntidadDocumento(sol.getUrlDniTesorero(), TipoDocumento.DNI_TESORERO, org, null);
        crearEntidadDocumento(sol.getUrlCertificadoResidenciaTesorero(), TipoDocumento.CERTIFICADO_RESIDENCIA_TESORERO, org, null);

        // 5. Actualizar la tabla temporal (queda como historial)
        sol.setEstado(EstadoSolicitud.Aprobada);
        sol.setFechaAprobacion(LocalDate.now());
        sol.setMotivo("Aprobada por el Ministerio. Pasada a tablas oficiales.");
        solicitudOrgRepo.save(sol);

        // 6. Lógica de creación de Usuarios y envío de Mails[cite: 3]

        // -> Cuenta para el Presidente
        generarUsuarioYEnviarMail(
                sol.getNombrePresidente(),
                sol.getApellidoPresidente(),
                sol.getCorreoPresidente(),
                org.getNombre()
        );

        // -> Cuenta para el Tesorero (Si la ONG lo registró)
        if (sol.getDniTesorero() != null) {
            generarUsuarioYEnviarMail(
                    sol.getNombreTesorero(),
                    sol.getApellidoTesorero(),
                    sol.getCorreoTesorero(),
                    org.getNombre()
            );
        }
    }

//METODO RECHAZAR V2
@Override
@Transactional
public void rechazarOrganizacion(String cuit, String motivo) {
    SolicitudOrganizacion sol = solicitudOrgRepo.findByCuit(cuit)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada."));

    if (sol.getEstado() == EstadoSolicitud.Aprobada) throw new RuntimeException("No se puede rechazar una ONG ya aprobada.");

    // 1. Borramos los archivos pesados del servidor
    borrarArchivoFisico(sol.getUrlNotaSolicitud());
    borrarArchivoFisico(sol.getUrlAltaAfip());
    borrarArchivoFisico(sol.getUrlActaAsamblea());
    borrarArchivoFisico(sol.getUrlActaCompromiso());
    borrarArchivoFisico(sol.getUrlConstanciaVigencia());
    borrarArchivoFisico(sol.getUrlConstanciaCuentaBancaria());
    borrarArchivoFisico(sol.getUrlDniPresidente());
    borrarArchivoFisico(sol.getUrlCertificadoResidenciaPresidente());
    borrarArchivoFisico(sol.getUrlDniTesorero());
    borrarArchivoFisico(sol.getUrlCertificadoResidenciaTesorero());

    // 2. Limpiamos las rutas en la BD para que no apunten a la nada, pero conservamos el motivo
    sol.setUrlNotaSolicitud(null);
    sol.setUrlAltaAfip(null);
    sol.setUrlActaAsamblea(null);
    sol.setUrlActaCompromiso(null);
    sol.setUrlConstanciaVigencia(null);
    sol.setUrlConstanciaCuentaBancaria(null);
    sol.setUrlDniPresidente(null);
    sol.setUrlCertificadoResidenciaPresidente(null);
    sol.setUrlDniTesorero(null);
    sol.setUrlCertificadoResidenciaTesorero(null);

    sol.setEstado(EstadoSolicitud.Rechazada);
    sol.setMotivo(motivo);
    sol.setFechaRechazo(LocalDate.now());
    solicitudOrgRepo.save(sol);

    // 3. Notificamos al correo del presidente usando el servicio que armó Marian
    if (sol.getCorreoPresidente() != null && sol.getCorreoPresidente().contains("@")) {
        emailService.enviarNotificacionSolicitudRechazada(sol.getCorreoPresidente(), sol.getNombre(), motivo);
    }
}

    // =============================================
    // FORMULARIO 2: SOLICITUD DE VIVIENDA (familia)
    // =============================================


    //METODO V2 REGISTRAR SOLICITUD FAMILIA
    @Override
    @Transactional
    public SolicitudDTO registrarSolicitudFamilia(RegistroFamiliaDTO dto,
                                                  MultiValueMap<String, MultipartFile> mapaArchivos) {

        // Validar que la org que presenta exista
        Organizacion org = orgRepo.findById(dto.getCuitOrg())
                .orElseThrow(() -> new RuntimeException("Organización no encontrada: " + dto.getCuitOrg()));

        // Guardar familia y sus familiares
        Familia familia = familiaRepo.save(mapper.toEntity(dto.getFamilia()));

        if (dto.getFamiliares() != null) {
            for (FamiliarDTO familiarDTO : dto.getFamiliares()) {
                Familiar familiar = mapper.toEntity(familiarDTO);
                familiar.setFamilia(familia);
                familiarRepo.save(familiar);
            }
        }

        // Crear la solicitud — el back arma todo, el front no manda nada de esto
        Solicitud solicitud = new Solicitud();
        solicitud.setCuitOrg(org);
        solicitud.setFamiliaBeneficiaria(familia);
        solicitud.setEstado(EstadoSolicitud.Pendiente);
        solicitud.setObservacion(dto.getObservacion());
        solicitud.setFechaSolicitud(LocalDate.now());
        solicitud = solicitudRepo.save(solicitud);

        // Guardar archivos de la familia
        if (mapaArchivos != null) {
            mapaArchivos.forEach((clave, listaArchivos) -> {
                if (!clave.equals("datos") && !listaArchivos.isEmpty()) {
                    try {
                        TipoDocumento tipoDoc = TipoDocumento.valueOf(clave.toUpperCase());
                        for (MultipartFile archivo : listaArchivos) {

                            // 1. Guardamos el archivo físico y obtenemos la ruta
                            String rutaFisica = guardarArchivoFisico(archivo);

                            // 2. Creamos la entidad Documento oficial en la BD (org es null acá)
                            crearEntidadDocumento(rutaFisica, tipoDoc, null, familia);

                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("Clave de documento ignorada: " + clave);
                    }
                }
            });
        }
        return mapper.toDTO(solicitud);
    }
    // =============================================
    // APROBAR SOLICITUD DE VIVIENDA
    // =============================================
    @Override
    @Transactional
    public SolicitudDTO aprobarSolicitudVivienda(Integer idSolicitud, String numExp) {
        Solicitud s = solicitudRepo.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + idSolicitud));

        if (s.getEstado() == EstadoSolicitud.Aprobada)
            throw new RuntimeException("La solicitud ya está aprobada");

        Vivienda vivienda = viviendaRepo.findViviendaByNumExp(numExp)
                .orElseThrow(() -> new RuntimeException("Expediente no encontrado: " + numExp));

        s.setEstado(EstadoSolicitud.Aprobada);
        s.setNumExp(vivienda);
        s.setFechaActivacion(LocalDate.now());

        return mapper.toDTO(solicitudRepo.save(s));
    }

    // =============================================
    // RECHAZAR SOLICITUD DE VIVIENDA
    // =============================================
    @Override
    @Transactional
    public void rechazarSolicitudVivienda(Integer idSolicitud, String motivo) {
        Solicitud s = solicitudRepo.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + idSolicitud));

        if (s.getEstado() == EstadoSolicitud.Aprobada)
            throw new RuntimeException("No se puede rechazar una solicitud ya aprobada");

        s.setEstado(EstadoSolicitud.Rechazada);
        s.setObservacion(motivo);

        solicitudRepo.save(s);
    }

    @Override
    @Transactional
    public SolicitudDTO semiAprobarSolicitud(Integer idSolicitud, String motivo) {
        Solicitud s = solicitudRepo.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + idSolicitud));

        if (s.getEstado() == EstadoSolicitud.Aprobada)
            throw new RuntimeException("No se puede modificar una solicitud ya aprobada");

        if (s.getEstado() == EstadoSolicitud.Rechazada)
            throw new RuntimeException("No se puede modificar una solicitud ya rechazada");

        s.setEstado(EstadoSolicitud.SemiAprobada);
        s.setObservacion(motivo); // acá el operador especifica qué documento está mal

        return mapper.toDTO(solicitudRepo.save(s));
    }

    // =========================================================================
    // MÉTODOS AUXILIARES PRIVADOS (Manejo de Archivos)
    // =========================================================================

    //METODO V2
    private String guardarArchivoFisico(MultipartFile archivo) {
        // 1. Validamos que no esté vacío
        if (archivo == null || archivo.isEmpty()) {
            throw new RuntimeException("El archivo llegó vacío o corrupto desde el frontend.");
        }

        // 2. Pasamos el archivo por tu validador de peso y extensión
        validarArchivo(archivo);

        try {
            // 3. Preparamos la carpeta
            Path directorio = Paths.get(CARPETA_UPLOADS);
            if (!Files.exists(directorio)) {
                Files.createDirectories(directorio);
            }

            // 4. Generamos un nombre único
            String originalName = archivo.getOriginalFilename();
            String ext = originalName.substring(originalName.lastIndexOf("."));
            String nombreUnico = UUID.randomUUID().toString() + ext;

            // 5. Armamos la ruta final y copiamos el archivo
            Path rutaDestino = directorio.resolve(nombreUnico);
            Files.copy(archivo.getInputStream(), rutaDestino);

            // 6. Devolvemos el String con la ruta para guardarlo en la tabla temporal
            return rutaDestino.toString();

        } catch (IOException e) {
            throw new RuntimeException("Error físico al guardar el archivo en el servidor: " + e.getMessage(), e);
        }
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo.getSize() > MAX_SIZE) {
            throw new RuntimeException("El archivo '" + archivo.getOriginalFilename() + "' supera el máximo de 20MB");
        }
        String originalName = archivo.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new RuntimeException("El archivo '" + originalName + "' no tiene extensión válida");
        }
        String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        if (!EXTENSIONES.contains(ext)) {
            throw new RuntimeException("Extensión no permitida (." + ext + "). Solo PDF, JPG, JPEG o PNG.");
        }
    }

    //NUEVO METODO PARA BORRAR DOCUMENTOS FISICOS
    private void borrarArchivoFisico(String url) {
        if (url != null && !url.isEmpty()) {
            try {
                Files.deleteIfExists(Paths.get(url));
            } catch (IOException e) {
                log.error("No se pudo borrar el archivo: {}", url);
            }
        }
    }

    //NUEVO METODO PARA CREAR LA ENTIDAD DOCUMENTO
    private void crearEntidadDocumento(String url, TipoDocumento tipo, Organizacion org, Familia familia) {
        if (url != null && !url.isEmpty()) {
            Documento doc = new Documento();
            doc.setNombre(Paths.get(url).getFileName().toString());
            doc.setUrl(url);
            doc.setTipo(tipo);
            doc.setEstado(EstadoDocumento.PENDIENTE);
            doc.setOrganizacion(org);
            doc.setFamilia(familia);
            docRepo.save(doc);
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
