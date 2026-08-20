package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.DocumentoDTO;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.Documento;
import com.vivso.Vivso.Model.EstadoDocumento;
import com.vivso.Vivso.Model.TipoDocumento;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Repository.IDocumentoRepository;
import com.vivso.Vivso.Repository.IFamiliaRepository;
import com.vivso.Vivso.Repository.IOrganizacionRepository;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class DocumentoService implements IDocumentoService {

    @Autowired private IDocumentoRepository docRepo;
    @Autowired private IFamiliaRepository familiaRepo;
    @Autowired private IOrganizacionRepository orgRepo;
    @Autowired private IUsuarioRepository usuarioRepo;
    @Autowired private VivsoMapper mapper;

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
    public DocumentoDTO revisar(Integer idDoc, Integer idRevisor, EstadoDocumento nuevoEstado, String motivo) {
        Documento doc = docRepo.findById(idDoc)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + idDoc));

        Usuario revisor = usuarioRepo.findById(idRevisor)
                .orElseThrow(() -> new RuntimeException("El revisor es obligatorio para validar"));

        doc.setEstado(nuevoEstado);
        doc.setRevisor(revisor);
        doc.setMotivoRechazo(nuevoEstado == EstadoDocumento.RECHAZADO ? motivo : null);

        doc.setFechaRevision(LocalDateTime.now());

        return mapper.toDTO(docRepo.save(doc));
    }

    @Override
    @Transactional
    public DocumentoDTO reemplazar(Integer idDoc, MultipartFile archivo) {
        Documento doc = docRepo.findById(idDoc)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + idDoc));

        if (doc.getEstado() != EstadoDocumento.RECHAZADO) {
            throw new RuntimeException("Solo se pueden reemplazar documentos rechazados");
        }

        validarArchivo(archivo, doc.getNombre());

        try {
            Path directorio = Paths.get(rootFolder);
            if (!Files.exists(directorio)) Files.createDirectories(directorio);

            String originalName = archivo.getOriginalFilename();
            if (originalName == null || !originalName.contains("."))
                throw new RuntimeException("El archivo no tiene extensión válida.");

            Path rutaDestino = directorio.resolve(
                    UUID.randomUUID().toString() + originalName.substring(originalName.lastIndexOf(".")));

            Files.copy(archivo.getInputStream(), rutaDestino);

            doc.setNombre(originalName);
            doc.setUrl(rutaDestino.toString());
            doc.setEstado(EstadoDocumento.PENDIENTE);
            doc.setMotivoRechazo(null);
            doc.setRevisor(null);

            return mapper.toDTO(docRepo.save(doc));

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + archivo.getOriginalFilename(), e);
        }
    }

    @Override
    public List<DocumentoDTO> listarPorFamilia(Integer idFamilia) {
        if (!familiaRepo.existsById(idFamilia))
            throw new RuntimeException("No se encontró la familia: " + idFamilia);
        return docRepo.findByFamilia_IdFamilia(idFamilia).stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<DocumentoDTO> listarPorOrganizacion(String cuitOrg) {
        if (!orgRepo.existsById(cuitOrg))
            throw new RuntimeException("No se encontró la organización: " + cuitOrg);
        return docRepo.findByOrganizacion_Cuit(cuitOrg).stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<DocumentoDTO> listarPorUsuarioRevisor(Integer idUsuario) {
        if (!usuarioRepo.existsById(idUsuario))
            throw new RuntimeException("No se encontró el revisor: " + idUsuario);
        return docRepo.findByRevisor_Id(idUsuario).stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public void eliminar(Integer id) {
        docRepo.deleteById(id);
    }

    private void validarArchivo(MultipartFile archivo, String nombreCampo) {
        if (archivo == null || archivo.isEmpty()) return;

        // Validar tamaño
        if (archivo.getSize() > MAX_SIZE_BYTES) {
            throw new RuntimeException(
                    "El archivo '" + nombreCampo + "' supera el máximo de 20MB " +
                            "(peso actual: " + archivo.getSize() / (1024 * 1024) + "MB)"
            );
        }

        // Validar extensión
        String nombreOriginal = archivo.getOriginalFilename();
        if (nombreOriginal == null || !nombreOriginal.contains(".")) {
            throw new RuntimeException("El archivo '" + nombreCampo + "' no tiene una extensión válida");
        }

        String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf('.') + 1).toLowerCase();
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new RuntimeException(
                    "El archivo '" + nombreCampo + "' tiene extensión no permitida (." + extension + "). " +
                            "Solo se aceptan: PDF, JPG, PNG"
            );
        }
    }

}