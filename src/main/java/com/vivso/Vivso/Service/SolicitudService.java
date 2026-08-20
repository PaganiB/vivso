package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.*;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.*;
import com.vivso.Vivso.Repository.*;
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
import java.util.UUID;

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
    @Override
    @Transactional
    public void registrarOrganizacion(RegistroOrganizacionDTO dto,
                                      MultiValueMap<String, MultipartFile> mapaArchivos) {

        // Validar que el CUIT no exista ya
        if (orgRepo.existsById(dto.getOrganizacion().getCuit())) {
            throw new RuntimeException("El CUIT ya está registrado: " + dto.getOrganizacion().getCuit());
        }

        // Guardar org e integrantes
        Organizacion org = orgRepo.save(mapper.toEntity(dto.getOrganizacion()));

        Integrante presidente = mapper.toEntity(dto.getPresidente());
        presidente.setOrganizacion(org);
        integranteRepo.save(presidente);

        if (dto.getTesorero() != null) {
            Integrante tesorero = mapper.toEntity(dto.getTesorero());
            tesorero.setOrganizacion(org);
            integranteRepo.save(tesorero);
        }

        // LOG TEMPORAL
        System.out.println("=== CLAVES RECIBIDAS ===");
        if (mapaArchivos != null) {
            mapaArchivos.forEach((clave, lista) ->
                    System.out.println("Clave: '" + clave + "' | Archivos: " + lista.size() + " | Tamaño: " + lista.get(0).getSize())
            );
        } else {
            System.out.println("mapaArchivos es NULL");
        }

        // Guardar archivos de la org (familia = null porque acá no hay familia)
        if (mapaArchivos != null) {
            mapaArchivos.forEach((clave, listaArchivos) -> {
                if (!clave.equals("datos")) {
                    try {
                        TipoDocumento tipoDoc = TipoDocumento.valueOf(clave.toUpperCase());
                        for (MultipartFile archivo : listaArchivos) {
                            guardarSiPresente(archivo, tipoDoc, org, null);
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("Clave ignorada: " + clave);
                    }
                }
            });
        }
    }

    // Metodos para aceptar o rechazar el form de org
    @Override
    @Transactional
    public void aprobarOrganizacion(String cuit) {
        Organizacion org = orgRepo.findById(cuit)
                .orElseThrow(() -> new RuntimeException("Organización no encontrada: " + cuit));

        List<Integrante> integrantes = integranteRepo.findByOrganizacion_Cuit(cuit);
        if (integrantes.isEmpty())
            throw new RuntimeException("La organización no tiene integrantes registrados");

        for (Integrante integrante : integrantes) {
            // La contraseña es su DNI por ahora
            UsuarioRegistroDTO nuevoUsuario = UsuarioRegistroDTO.builder()
                    .username(integrante.getNombre() + "." + integrante.getApellido())
                    .email(integrante.getCorreo())
                    .password(integrante.getDni())
                    .rol("INTEGRANTE")
                    .build();

            usuarioService.registrarNuevoUsuario(nuevoUsuario);

            // TODO: mandar mail con credenciales al integrante
        }
    }
    @Override
    @Transactional
    public void rechazarOrganizacion(String cuit, String motivo) {
        Organizacion org = orgRepo.findByCuit(cuit)
                .orElseThrow(() -> new RuntimeException("Organización no encontrada: " + cuit));

        docRepo.deleteByOrganizacion_Cuit(cuit);
        integranteRepo.deleteByOrganizacion_Cuit(cuit);
        orgRepo.delete(org);
    }

    // =============================================
    // FORMULARIO 2: SOLICITUD DE VIVIENDA (familia)
    // =============================================
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
                if (!clave.equals("datos")) {
                    try {
                        TipoDocumento tipoDoc = TipoDocumento.valueOf(clave.toUpperCase());
                        for (MultipartFile archivo : listaArchivos) {
                            guardarSiPresente(archivo, tipoDoc, null, familia);
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("Clave ignorada: " + clave);
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

    private void guardarSiPresente(MultipartFile archivo, TipoDocumento tipo, Organizacion org, Familia familia) {

        // 1. BARRERA DE ENTRADA: Si está nulo o vacío, EXPLOTA Y CORTA TODO
        if (archivo == null || archivo.isEmpty()) {
            System.out.println("=== ARCHIVO VACÍO O NULL DETECTADO. TIPO: " + tipo + " ===");
            throw new RuntimeException("El archivo para '" + tipo.name() + "' llegó vacío (0 bytes) o corrupto desde el frontend.");
        }

        // 2. Validar peso y extensión
        validarArchivo(archivo);

        try {
            // 3. Preparar directorio
            Path directorio = Paths.get(CARPETA_UPLOADS);
            if (!Files.exists(directorio)) {
                Files.createDirectories(directorio);
            }

            // 4. Nombre seguro (Aseguramos que no falle si no tiene extensión)
            String originalName = archivo.getOriginalFilename();
            if (originalName == null || !originalName.contains(".")) {
                throw new RuntimeException("El archivo '" + tipo.name() + "' no tiene una extensión válida.");
            }
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String nombreUnico = UUID.randomUUID().toString() + extension;
            Path rutaDestino = directorio.resolve(nombreUnico);

            // 5. Guardado físico en disco
            Files.copy(archivo.getInputStream(), rutaDestino);
            System.out.println("=== ARCHIVO GUARDADO EN DISCO: " + rutaDestino);

            // 6. Guardado en Base de Datos
            Documento doc = new Documento();
            doc.setNombre(originalName);
            doc.setUrl(rutaDestino.toString());
            doc.setTipo(tipo);
            doc.setOrganizacion(org);
            doc.setFamilia(familia);

            System.out.println("=== INTENTANDO GUARDAR EN BD: " + tipo + " | org: " + (org != null ? org.getCuit() : "null"));
            Documento guardado = docRepo.save(doc);

            System.out.println(
                    "=== GUARDADO OK === " +
                            "TIPO: " + guardado.getTipo() +
                            " | ID: " + guardado.getIdDocumento()
            );

        } catch (IOException e) {
            throw new RuntimeException("Error físico al guardar el archivo en disco: " + archivo.getOriginalFilename(), e);
        } catch (Exception e) {
            System.out.println("=== ERROR AL GUARDAR EN BD: " + e.getMessage());
            throw new RuntimeException("Error al guardar registro en BD: " + e.getMessage(), e);
        }
    }

    /* METODO NUEVO PARA PROBAR
    private void guardarSiPresente(MultipartFile archivo, TipoDocumento tipo, Organizacion org, Familia familia) {

        // Opcional: solo el certificado de discapacidad puede no venir
        if (archivo == null || archivo.isEmpty()) {
            if (tipo == TipoDocumento.CERTIFICADO_DISCAPACIDAD) return;
            throw new RuntimeException("El archivo '" + tipo.name() + "' es obligatorio y no fue recibido.");
        }

        validarArchivo(archivo);

        try {
            Path directorio = Paths.get(CARPETA_UPLOADS);
            if (!Files.exists(directorio)) Files.createDirectories(directorio);

            String originalName = archivo.getOriginalFilename();
            if (originalName == null || !originalName.contains("."))
                throw new RuntimeException("El archivo '" + tipo.name() + "' no tiene extensión válida.");

            Path rutaDestino = directorio.resolve(UUID.randomUUID().toString() +
                    originalName.substring(originalName.lastIndexOf(".")));

            Files.copy(archivo.getInputStream(), rutaDestino);

            Documento doc = new Documento();
            doc.setNombre(originalName);
            doc.setUrl(rutaDestino.toString());
            doc.setTipo(tipo);
            doc.setOrganizacion(org);
            doc.setFamilia(familia);
            docRepo.save(doc);

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + archivo.getOriginalFilename(), e);
        }
    }*/

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
}
