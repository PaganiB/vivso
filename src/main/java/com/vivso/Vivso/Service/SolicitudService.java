package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.SolicitudCompletaDTO;
import com.vivso.Vivso.DTO.SolicitudDTO;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.*;
import com.vivso.Vivso.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Autowired private IDocumentoRepository docRepo;
    @Autowired private VivsoMapper mapper;

    // Constantes para los archivos físicos
    private static final String CARPETA_UPLOADS = "uploads/documentos/";
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5 MB
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

    @Transactional
    public SolicitudDTO crearSolicitudCompleta(SolicitudCompletaDTO megaDto) {

        // 1. === GUARDAR EL TEXTO Y ARMAR RELACIONES ===

        // Validación de CUIT para evitar errores de duplicado en la BD
        if (orgRepo.existsById(megaDto.getOrganizacion().getCuit())) {
            throw new RuntimeException("El CUIT ya está registrado: " + megaDto.getOrganizacion().getCuit());
        }
        Organizacion org = orgRepo.save(mapper.toEntity(megaDto.getOrganizacion()));

        Integrante presidente = mapper.toEntity(megaDto.getPresidente());
        presidente.setOrganizacion(org);
        integranteRepo.save(presidente);

        if (megaDto.getTesorero() != null) {
            Integrante tesorero = mapper.toEntity(megaDto.getTesorero());
            tesorero.setOrganizacion(org);
            integranteRepo.save(tesorero);
        }

        Familia familia = familiaRepo.save(mapper.toEntity(megaDto.getFamilia()));

        Solicitud solicitud = mapper.toEntity(megaDto.getSolicitud());
        solicitud.setCuitOrg(org);
        solicitud.setFamiliaBeneficiaria(familia);
        if (solicitud.getFechaSolicitud() == null) {
            solicitud.setFechaSolicitud(LocalDate.now()); // Asegura fecha de hoy si no viene
        }
        solicitud = solicitudRepo.save(solicitud); // Genera el idSolicitud

        // 2. === GUARDAR LOS ARCHIVOS VINCULADOS ===

        // Documentos de la Organización
        guardarSiPresente(megaDto.getNotaSolicitud(), TipoDocumento.NOTA_SOLICITUD, org, familia);
        guardarSiPresente(megaDto.getActaAsamblea(), TipoDocumento.ACTA_DE_ASAMBLEA_ACTUAL_HCD, org, familia);
        guardarSiPresente(megaDto.getConstanciaCuentaBancaria(), TipoDocumento.CONSTANCIA_CUENTA_BANCARIA, org, familia);
        guardarSiPresente(megaDto.getAltaAfip(), TipoDocumento.ALTA_AFIP, org, familia);
        guardarSiPresente(megaDto.getDniPresidente(), TipoDocumento.DNI, org, familia);
        guardarSiPresente(megaDto.getDniTesorero(), TipoDocumento.DNI, org, familia);

        // Documentos de la Familia
        guardarSiPresente(megaDto.getCertificadoResidencia(), TipoDocumento.CERTIFICADO_RESIDENCIA, org, familia);
        guardarSiPresente(megaDto.getActaCompromiso(), TipoDocumento.ACTA_COMPROMISO, org, familia);
        guardarSiPresente(megaDto.getCertificadoDiscapacidad(), TipoDocumento.CERTIFICADO_DISCAPACIDAD, org, familia);
        guardarSiPresente(megaDto.getEscrituraPropiedad(), TipoDocumento.ESCRITURA_PROPIEDAD, org, familia);
        guardarSiPresente(megaDto.getDeclaracionJurada(), TipoDocumento.DECLARACION_JURADA_POSEEDOR, org, familia);

        // Listas de Archivos
        if (megaDto.getDniFamiliares() != null) {
            for (MultipartFile dni : megaDto.getDniFamiliares()) {
                guardarSiPresente(dni, TipoDocumento.DNI, org, familia);
            }
        }
        if (megaDto.getFotosTerreno() != null) {
            for (MultipartFile foto : megaDto.getFotosTerreno()) {
                guardarSiPresente(foto, TipoDocumento.FOTO_TERRENO, org, familia);
            }
        }

        return mapper.toDTO(solicitud);
    }

    // =========================================================================
    // MÉTODOS AUXILIARES PRIVADOS (Manejo de Archivos)
    // =========================================================================

    private void guardarSiPresente(MultipartFile archivo, TipoDocumento tipo, Organizacion org, Familia familia) {
        if (archivo == null || archivo.isEmpty()) {
            return; // Saltea si no adjuntaron este archivo específico
        }

        validarArchivo(archivo);

        try {
            // Crea la carpeta si no existe en el sistema
            Path directorio = Paths.get(CARPETA_UPLOADS);
            if (!Files.exists(directorio)) {
                Files.createDirectories(directorio);
            }

            // Genera nombre único con UUID para evitar sobreescrituras
            String originalName = archivo.getOriginalFilename();
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String nombreUnico = UUID.randomUUID().toString() + extension;
            Path rutaDestino = directorio.resolve(nombreUnico);

            // Guarda físicamente en el disco
            Files.copy(archivo.getInputStream(), rutaDestino);

            // Guarda el registro en la BD
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
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo.getSize() > MAX_SIZE) {
            throw new RuntimeException("El archivo '" + archivo.getOriginalFilename() + "' supera el máximo de 5MB");
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
