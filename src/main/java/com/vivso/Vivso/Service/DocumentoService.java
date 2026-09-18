package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.DocumentoDTO;
import com.vivso.Vivso.Exception.RecursoNoEncontradoException;
import com.vivso.Vivso.Exception.ReglaDeNegocioException;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.*;
import com.vivso.Vivso.Repository.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class DocumentoService implements IDocumentoService {

    @Autowired private IDocumentoRepository docRepo;
    @Autowired private IFamiliaRepository familiaRepo;
    @Autowired private IOrganizacionRepository orgRepo;
    @Autowired private IUsuarioRepository usuarioRepo;
    @Autowired private VivsoMapper mapper;
    @Autowired private ISolicitudOrganizacionRepository  solicitudOrgRepo;


    private final String rootFolder = "uploads/documentos";
    private static final long MAX_SIZE_BYTES = 20 * 1024 * 1024; // 20MB
    private static final List<String> EXTENSIONES_PERMITIDAS = List.of("pdf", "jpg", "jpeg", "png");

    @Override
    @Transactional
    public DocumentoDTO subirDocumento(MultipartFile archivo, TipoDocumento tipo, Integer idFamilia, String cuitOrg) {
        try {
            Path pathRoot = Paths.get(rootFolder);
            if (!Files.exists(pathRoot)) Files.createDirectories(pathRoot);

            String nombreArchivo = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
            Path destino = pathRoot.resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            Documento doc = new Documento();
            doc.setNombre(archivo.getOriginalFilename());
            doc.setUrl(destino.toString());
            doc.setTipo(tipo);
            doc.setEstado(EstadoDocumento.PENDIENTE);

            if (idFamilia != null) doc.setFamilia(familiaRepo.findById(idFamilia).orElse(null));
            if (cuitOrg   != null) doc.setOrganizacion(orgRepo.findById(cuitOrg).orElse(null));

            return mapper.toDTO(docRepo.save(doc));

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public DocumentoDTO marcarParaCorregir(Integer idDoc, String motivo) {

        Documento doc = docRepo.findById(idDoc)
                .orElseThrow(() -> new RecursoNoEncontradoException("Documento no encontrado: " + idDoc));

        if (doc.getEstado() != EstadoDocumento.PENDIENTE) {
            throw new ReglaDeNegocioException("Solo se pueden revisar documentos pendientes", HttpStatus.CONFLICT);
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario revisor = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario autenticado no encontrado"));

        doc.setEstado(EstadoDocumento.A_CORREGIR);
        doc.setRevisor(revisor);
        doc.setMotivoObservacion(motivo);
        doc.setFechaRevision(LocalDateTime.now());

        return mapper.toDTO(docRepo.save(doc));
    }

    @Override
    @Transactional
    public DocumentoDTO reemplazar(Integer idDoc, MultipartFile archivo) {
        Documento doc = docRepo.findById(idDoc)
                .orElseThrow(() -> new RecursoNoEncontradoException("Documento no encontrado: " + idDoc));

        if (doc.getEstado() != EstadoDocumento.A_CORREGIR) {
            throw new ReglaDeNegocioException("Solo se pueden reemplazar documentos marcados para corregir", HttpStatus.CONFLICT);
        }

        validarArchivo(archivo, doc.getNombre());

        try {
            Path directorio = Paths.get(rootFolder);
            if (!Files.exists(directorio)) Files.createDirectories(directorio);

            String originalName = archivo.getOriginalFilename();
            if (originalName == null || !originalName.contains("."))
                throw new ReglaDeNegocioException("El archivo no tiene extensión válida.", HttpStatus.BAD_REQUEST);

            Path rutaDestino = directorio.resolve(
                    UUID.randomUUID().toString() + originalName.substring(originalName.lastIndexOf(".")));

            Files.copy(archivo.getInputStream(), rutaDestino);

            doc.setNombre(originalName);
            doc.setUrl(rutaDestino.toString());
            doc.setEstado(EstadoDocumento.PENDIENTE);
            doc.setMotivoObservacion(null);
            doc.setRevisor(null);

            return mapper.toDTO(docRepo.save(doc));

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + archivo.getOriginalFilename(), e);
        }
    }

    @Override
    public List<DocumentoDTO> listarPorFamilia(Integer idFamilia) {
        if (!familiaRepo.existsById(idFamilia))
            throw new RecursoNoEncontradoException("No se encontró la familia: " + idFamilia);
        return docRepo.findByFamilia_IdFamilia(idFamilia).stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<DocumentoDTO> listarPorOrganizacion(String cuitOrg) {
        if (!orgRepo.existsById(cuitOrg))
            throw new RecursoNoEncontradoException("No se encontró la organización: " + cuitOrg);
        return docRepo.findByOrganizacion_Cuit(cuitOrg).stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<DocumentoDTO> listarPorUsuarioRevisor(Integer idUsuario) {
        if (!usuarioRepo.existsById(idUsuario))
            throw new RecursoNoEncontradoException("No se encontró el revisor: " + idUsuario);
        return docRepo.findByRevisor_Id(idUsuario).stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Documento doc = docRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Documento no encontrado: " + id));

        borrarArchivoFisico(doc.getUrl());
        docRepo.deleteById(id);
    }

    private void validarArchivo(MultipartFile archivo, String nombreCampo) {
        if (archivo == null || archivo.isEmpty()) return;

        // Validar tamaño
        if (archivo.getSize() > MAX_SIZE_BYTES) {
            throw new ReglaDeNegocioException(
                    "El archivo '" + nombreCampo + "' supera el máximo de 20MB (peso actual: " + archivo.getSize() / (1024 * 1024) + "MB)",
                    HttpStatus.BAD_REQUEST);
        }

        // Validar extensión
        String nombreOriginal = archivo.getOriginalFilename();
        if (nombreOriginal == null || !nombreOriginal.contains(".")) {
            throw new ReglaDeNegocioException("El archivo '" + nombreCampo + "' no tiene una extensión válida", HttpStatus.BAD_REQUEST);
        }

        String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf('.') + 1).toLowerCase();
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new ReglaDeNegocioException(
                    "El archivo '" + nombreCampo + "' tiene extensión no permitida (." + extension + "). Solo se aceptan: PDF, JPG, PNG",
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public String guardarArchivoFisico(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ReglaDeNegocioException("El archivo llegó vacío o corrupto desde el frontend.", HttpStatus.BAD_REQUEST);
        }
        validarArchivo(archivo, archivo.getOriginalFilename());

        try {
            Path pathRoot = Paths.get(rootFolder);
            if (!Files.exists(pathRoot)) Files.createDirectories(pathRoot);

            String originalName = archivo.getOriginalFilename();
            String ext = originalName.substring(originalName.lastIndexOf("."));
            String nombreUnico = UUID.randomUUID().toString() + ext;

            Path rutaDestino = pathRoot.resolve(nombreUnico);
            Files.copy(archivo.getInputStream(), rutaDestino);

            return rutaDestino.toString();
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public DocumentoDTO crearDesdeRutaExistente(String url, TipoDocumento tipo, Integer idFamilia, String cuitOrg) {
        Documento doc = new Documento();
        doc.setNombre(Paths.get(url).getFileName().toString());
        doc.setUrl(url);
        doc.setTipo(tipo);
        doc.setEstado(EstadoDocumento.PENDIENTE);

        if (idFamilia != null) doc.setFamilia(familiaRepo.findById(idFamilia).orElse(null));
        if (cuitOrg != null) doc.setOrganizacion(orgRepo.findById(cuitOrg).orElse(null));

        return mapper.toDTO(docRepo.save(doc));
    }

    @Override
    public void borrarArchivoFisico(String url) {
        if (url != null && !url.isEmpty()) {
            try {
                Files.deleteIfExists(Paths.get(url));
            } catch (Exception e) {
                log.error("No se pudo borrar el archivo físico: {}", url);
            }
        }
    }

    @Override
    @Transactional
    public DocumentoDTO subirDocumentoDeSolicitudOrg(MultipartFile archivo, TipoDocumento tipo, Integer idSolicitudOrg) {
        String ruta = guardarArchivoFisico(archivo); // reutiliza el que ya existe, valida tamaño/extensión

        Documento doc = new Documento();
        doc.setNombre(archivo.getOriginalFilename());
        doc.setUrl(ruta);
        doc.setTipo(tipo);
        doc.setEstado(EstadoDocumento.PENDIENTE);
        doc.setSolicitudOrganizacion(
                solicitudOrgRepo.findById(idSolicitudOrg)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud de organización no encontrada: " + idSolicitudOrg))
        );

        return mapper.toDTO(docRepo.save(doc));
    }

    @Override
    public List<DocumentoDTO> listarPorSolicitudOrg(Integer idSolicitudOrg) {
        return docRepo.findBySolicitudOrganizacion_IdSolicitudOrg(idSolicitudOrg).stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public void aprobarConOrganizacion(Integer idDocumento, Organizacion org, Usuario revisor) {
        Documento doc = docRepo.findById(idDocumento)
                .orElseThrow(() -> new RecursoNoEncontradoException("Documento no encontrado: " + idDocumento));
        doc.setEstado(EstadoDocumento.APROBADO);
        doc.setOrganizacion(org);
        doc.setRevisor(revisor);
        docRepo.save(doc);
    }

    @Override
    @Transactional
    public void rechazar(Integer idDocumento, Usuario revisor) {
        Documento doc = docRepo.findById(idDocumento)
                .orElseThrow(() -> new RecursoNoEncontradoException("Documento no encontrado: " + idDocumento));
        borrarArchivoFisico(doc.getUrl());
        doc.setEstado(EstadoDocumento.RECHAZADO);
        doc.setRevisor(revisor);
        docRepo.save(doc);
    }

}