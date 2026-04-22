package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.DocumentoDTO;
import com.vivso.Vivso.Mapper.Mapper;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentoService implements IDocumentoService {
    @Autowired private IDocumentoRepository docRepo;
    @Autowired private IFamiliaRepository familiaRepo;
    @Autowired private IOrganizacionRepository orgRepo;
    @Autowired private IUsuarioRepository usuarioRepo;

    private final String rootFolder = "uploads/documentos"; // Carpeta relativa al proyecto

    @Override
    @Transactional
    public DocumentoDTO subirDocumento(MultipartFile archivo, TipoDocumento tipo, Integer idFamilia, String cuitOrg) {
        try {
            // 1. Creamos la carpeta si no existe
            Path pathRoot = Paths.get(rootFolder);
            if (!Files.exists(pathRoot)) Files.createDirectories(pathRoot);

            // 2. Generamos un nombre único para que no se pisen archivos con el mismo nombre
            String nombreArchivo = UUID.randomUUID().toString() + "_" + archivo.getOriginalFilename();
            Path destino = pathRoot.resolve(nombreArchivo);

            // 3. Guardamos el archivo físicamente
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            // 4. Creamos la entidad
            Documento doc = new Documento();
            doc.setNombreOriginal(archivo.getOriginalFilename());
            doc.setUrlPath(destino.toString());
            doc.setTipo(tipo);
            doc.setEstado(EstadoDocumento.PENDIENTE); // Siempre nace pendiente

            // Vinculamos a quién pertenece
            if (idFamilia != null) doc.setFamilia(familiaRepo.findById(idFamilia).orElse(null));
            if (cuitOrg != null) doc.setOrganizacion(orgRepo.findById(cuitOrg).orElse(null));

            return Mapper.toDTO(docRepo.save(doc));

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo físico: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public DocumentoDTO revisar(Integer idDoc, Integer idRevisor, EstadoDocumento nuevoEstado, String motivo) {
        Documento doc = docRepo.findById(idDoc)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        Usuario revisor = usuarioRepo.findById(idRevisor)
                .orElseThrow(() -> new RuntimeException("El revisor es obligatorio para validar"));

        doc.setEstado(nuevoEstado);
        doc.setRevisor(revisor);
        doc.setMotivoRechazo(nuevoEstado == EstadoDocumento.RECHAZADO ? motivo : null);

        return Mapper.toDTO(docRepo.save(doc));
    }

    @Override
    public List<DocumentoDTO> listarPorFamilia(Integer idFamilia) {
        if (!familiaRepo.existsById(idFamilia)) {
            throw new RuntimeException("No se encontró la familia con ID: " + idFamilia);
        }

        // Ahora sí, traemos los documentos y mapeamos
        return docRepo.findByFamilia_IdFamilia(idFamilia).stream() // Cambié el nombre según tu entidad
                .map(Mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentoDTO> listarPorOrganizacion(String cuitOrg) {
        if (!orgRepo.existsById(cuitOrg)) {
            throw new RuntimeException("No se encontró la organización con CUIT: " + cuitOrg);
        }

        return docRepo.findByOrganizacion_Cuit(cuitOrg).stream()
                .map(Mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentoDTO> listarPorUsuarioRevisor(Integer idUsuario) {
        if (!usuarioRepo.existsById(idUsuario)) {
            throw new RuntimeException("No se encontró el usuario revisor con ID: " + idUsuario);
        }

        return docRepo.findByRevisor_Id(idUsuario).stream()
                .map(Mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        // Aquí podrías agregar lógica para borrar el archivo físico también
        docRepo.deleteById(id);
    }
}
