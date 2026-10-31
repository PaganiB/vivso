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
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class DocumentoService implements IDocumentoService {

  private static final long MAX_SIZE_BYTES = 20 * 1024 * 1024; // 20MB
  private static final List<String> EXTENSIONES_PERMITIDAS = List.of("pdf", "jpg", "jpeg", "png");
  @Autowired
  private IDocumentoRepository docRepo;
  @Autowired
  private IFamiliaRepository familiaRepo;
  @Autowired
  private IOrganizacionRepository orgRepo;
  @Autowired
  private IUsuarioRepository usuarioRepo;
  @Autowired
  private VivsoMapper mapper;
  @Autowired
  private ISolicitudOrganizacionRepository solicitudOrgRepo;
  @Autowired
  private CloudinaryService cloudinaryService;
  @Autowired
  private IFamiliarRepository familiarRepo;

  @Override
  @Transactional
  public DocumentoDTO subirDocumento(
          MultipartFile archivo,
          TipoDocumento tipo,
          Integer idFamilia,
          String cuitOrg) {

    return subirDocumento(
            archivo,
            tipo,
            idFamilia,
            cuitOrg,
            null
    );
  }

  @Override
  @Transactional
  public DocumentoDTO subirDocumento(
          MultipartFile archivo,
          TipoDocumento tipo,
          Integer idFamilia,
          String cuitOrg,
          Integer idFamiliar) {

    validarArchivo(archivo, archivo.getOriginalFilename());

    Documento doc = new Documento();
    doc.setNombre(archivo.getOriginalFilename());
    doc.setTipo(tipo);
    doc.setEstado(EstadoDocumento.PENDIENTE);

    // Asociar familia
    if (idFamilia != null) {
      Familia familia = familiaRepo.findById(idFamilia)
              .orElseThrow(() -> new ReglaDeNegocioException(
                      "La familia indicada no existe",
                      HttpStatus.NOT_FOUND
              ));

      doc.setFamilia(familia);
    }

    // Asociar familiar específico
    if (idFamiliar != null) {
      Familiar familiar = familiarRepo.findById(idFamiliar)
              .orElseThrow(() -> new ReglaDeNegocioException(
                      "El familiar indicado no existe",
                      HttpStatus.NOT_FOUND
              ));

      // Verificar que pertenezca a la familia indicada
      if (idFamilia == null ||
              !familiar.getFamilia().getIdFamilia().equals(idFamilia)) {

        throw new ReglaDeNegocioException(
                "El familiar no pertenece a la familia indicada",
                HttpStatus.BAD_REQUEST
        );
      }

      doc.setFamiliar(familiar);
    }

    // Asociar organización
    if (cuitOrg != null) {
      Organizacion organizacion = orgRepo.findById(cuitOrg)
              .orElseThrow(() -> new ReglaDeNegocioException(
                      "La organización indicada no existe",
                      HttpStatus.NOT_FOUND
              ));

      doc.setOrganizacion(organizacion);
    }

    // Subir archivo a Cloudinary
    String urlSegura = cloudinaryService.subirArchivo(
            archivo,
            "vivso_documentos"
    );

    doc.setUrl(urlSegura);

    return mapper.toDTO(docRepo.save(doc));
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

    cloudinaryService.borrarArchivo(doc.getUrl());

    String urlSegura = cloudinaryService.subirArchivo(archivo, "vivso_documentos");

    doc.setNombre(archivo.getOriginalFilename());
    doc.setUrl(urlSegura);
    doc.setEstado(EstadoDocumento.PENDIENTE);
    doc.setMotivoObservacion(null);
    doc.setRevisor(null);

    return mapper.toDTO(docRepo.save(doc));
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
    // Se mantiene este método público por si la interfaz lo requiere, pero delega la acción
    return cloudinaryService.subirArchivo(archivo, "vivso_documentos");
  }

  // Méttodo auxiliar para borrar archivos en Cloudinary
  private String extraerPublicId(String url) {
    try {
      String[] parts = url.split("/");
      String lastPart = parts[parts.length - 1]; // nombre_archivo.jpg
      String folder = parts[parts.length - 2];   // vivso_documentos
      String nombreSinExtension = lastPart.substring(0, lastPart.lastIndexOf("."));
      return folder + "/" + nombreSinExtension;
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public void borrarArchivoFisico(String url) {
    // Se mantiene este método público por si la interfaz lo requiere
    cloudinaryService.borrarArchivo(url);
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