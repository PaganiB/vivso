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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DocumentoService implements IDocumentoService {

  private static final long MAX_SIZE_BYTES = 20 * 1024 * 1024; // 20MB
  private static final List<String> EXTENSIONES_PERMITIDAS = List.of("pdf", "jpg", "jpeg", "png");

  @Autowired private IDocumentoRepository docRepo;
  @Autowired private IFamiliaRepository familiaRepo;
  @Autowired private IOrganizacionRepository orgRepo;
  @Autowired private IUsuarioRepository usuarioRepo;
  @Autowired private IIntegranteRepository integranteRepo; // Agregado para validación IDOR
  @Autowired private VivsoMapper mapper;
  @Autowired private ISolicitudOrganizacionRepository solicitudOrgRepo;
  @Autowired private CloudinaryService cloudinaryService;
  @Autowired private IFamiliarRepository familiarRepo;

  @Override
  public DocumentoDTO subirDocumento(MultipartFile archivo, TipoDocumento tipo, Integer idFamilia, String cuitOrg) {
    return subirDocumento(archivo, tipo, idFamilia, cuitOrg, null);
  }

  // =========================================================================================
  // 1. SUBIDA DESACOPLADA (Cloudinary I/O fuera de la transacción)
  // =========================================================================================
  @Override
  public DocumentoDTO subirDocumento(MultipartFile archivo, TipoDocumento tipo, Integer idFamilia, String cuitOrg, Integer idFamiliar) {
    validarArchivo(archivo, archivo.getOriginalFilename());

    // 1. Validaciones previas en memoria
    Familia familia = null;
    if (idFamilia != null) {
      familia = familiaRepo.findById(idFamilia)
        .orElseThrow(() -> new ReglaDeNegocioException("La familia indicada no existe", HttpStatus.NOT_FOUND));
    }

    Familiar familiar = null;
    if (idFamiliar != null) {
      familiar = familiarRepo.findById(idFamiliar)
        .orElseThrow(() -> new ReglaDeNegocioException("El familiar indicado no existe", HttpStatus.NOT_FOUND));
      if (idFamilia == null || !familiar.getFamilia().getIdFamilia().equals(idFamilia)) {
        throw new ReglaDeNegocioException("El familiar no pertenece a la familia indicada", HttpStatus.BAD_REQUEST);
      }
    }

    Organizacion organizacion = null;
    if (cuitOrg != null) {
      organizacion = orgRepo.findById(cuitOrg)
        .orElseThrow(() -> new ReglaDeNegocioException("La organización indicada no existe", HttpStatus.NOT_FOUND));
    }

    // 2. Operación de red lenta (Cloudinary) SIN bloquear la base de datos
    String urlSegura = cloudinaryService.subirArchivo(archivo, "vivso_documentos");

    // 3. Guardado en BD rápido y transaccional
    return guardarDocumentoEnBD(archivo.getOriginalFilename(), urlSegura, tipo, familia, familiar, organizacion);
  }

  @Transactional
  protected DocumentoDTO guardarDocumentoEnBD(String nombre, String url, TipoDocumento tipo, Familia familia, Familiar familiar, Organizacion organizacion) {
    Documento doc = new Documento();
    doc.setNombre(nombre);
    doc.setUrl(url);
    doc.setTipo(tipo);
    doc.setEstado(EstadoDocumento.PENDIENTE);
    doc.setFamilia(familia);
    doc.setFamiliar(familiar);
    doc.setOrganizacion(organizacion);
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

  // =========================================================================================
  // 2. REEMPLAZO BLINDADO (Sin @Transactional público + Prevención IDOR)
  // =========================================================================================
  @Override
  public DocumentoDTO reemplazar(Integer idDoc, MultipartFile archivo) {
    // 1. Buscamos y validamos propiedad (IDOR)
    Documento doc = buscarYValidarParaReemplazo(idDoc);
    validarArchivo(archivo, doc.getNombre());

    // 2. Operaciones de red (borrar y subir a Cloudinary)
    cloudinaryService.borrarArchivo(doc.getUrl());
    String urlSegura = cloudinaryService.subirArchivo(archivo, "vivso_documentos");

    // 3. Actualización rápida en BD
    return actualizarReemplazoEnBD(idDoc, archivo.getOriginalFilename(), urlSegura);
  }

  private Documento buscarYValidarParaReemplazo(Integer idDoc) {
    Documento doc = docRepo.findById(idDoc)
      .orElseThrow(() -> new RecursoNoEncontradoException("Documento no encontrado: " + idDoc));

    if (doc.getEstado() != EstadoDocumento.A_CORREGIR) {
      throw new ReglaDeNegocioException("Solo se pueden reemplazar documentos marcados para corregir", HttpStatus.CONFLICT);
    }

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
      String email = auth.getName();
      Optional<Integrante> integranteOpt = integranteRepo.findByUsuario_Email(email);

      if (integranteOpt.isPresent()) {
        String cuitUsuario = integranteOpt.get().getOrganizacion().getCuit();
        boolean lePertenece = (doc.getOrganizacion() != null && doc.getOrganizacion().getCuit().equals(cuitUsuario)) ||
          (doc.getSolicitudOrganizacion() != null && doc.getSolicitudOrganizacion().getCuit().equals(cuitUsuario));

        if (!lePertenece) {
          throw new ReglaDeNegocioException("No tienes permiso para reemplazar un documento ajeno.", HttpStatus.FORBIDDEN);
        }
      }
    }
    return doc;
  }

  @Transactional
  protected DocumentoDTO actualizarReemplazoEnBD(Integer idDoc, String nombreArchivo, String nuevaUrl) {
    Documento doc = docRepo.findById(idDoc).orElseThrow();
    doc.setNombre(nombreArchivo);
    doc.setUrl(nuevaUrl);
    doc.setEstado(EstadoDocumento.PENDIENTE);
    doc.setMotivoObservacion(null);
    doc.setRevisor(null);
    return mapper.toDTO(docRepo.save(doc));
  }

  // =========================================================================================
  // 3. ELIMINACIÓN DESACOPLADA
  // =========================================================================================
  @Override
  public void eliminar(Integer id) {
    Documento doc = docRepo.findById(id)
      .orElseThrow(() -> new RecursoNoEncontradoException("Documento no encontrado: " + id));
    borrarArchivoFisico(doc.getUrl());
    eliminarDeBD(id);
  }

  @Transactional
  protected void eliminarDeBD(Integer id) {
    docRepo.deleteById(id);
  }

  // =========================================================================================
  // 4. SUBIDA DE SOLICITUDES DESACOPLADA
  // =========================================================================================
  @Override
  public DocumentoDTO subirDocumentoDeSolicitudOrg(MultipartFile archivo, TipoDocumento tipo, Integer idSolicitudOrg) {
    SolicitudOrganizacion sol = solicitudOrgRepo.findById(idSolicitudOrg)
      .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud de organización no encontrada: " + idSolicitudOrg));

    String ruta = guardarArchivoFisico(archivo);

    return guardarDocSolicitudOrgEnBD(archivo.getOriginalFilename(), ruta, tipo, sol);
  }

  @Transactional
  protected DocumentoDTO guardarDocSolicitudOrgEnBD(String nombre, String ruta, TipoDocumento tipo, SolicitudOrganizacion sol) {
    Documento doc = new Documento();
    doc.setNombre(nombre);
    doc.setUrl(ruta);
    doc.setTipo(tipo);
    doc.setEstado(EstadoDocumento.PENDIENTE);
    doc.setSolicitudOrganizacion(sol);
    return mapper.toDTO(docRepo.save(doc));
  }

  // =========================================================================================
  // 5. RECHAZO DESACOPLADO
  // =========================================================================================
  @Override
  public void rechazar(Integer idDocumento, Usuario revisor) {
    Documento doc = docRepo.findById(idDocumento)
      .orElseThrow(() -> new RecursoNoEncontradoException("Documento no encontrado: " + idDocumento));
    borrarArchivoFisico(doc.getUrl());
    marcarRechazadoEnBD(idDocumento, revisor);
  }

  @Transactional
  protected void marcarRechazadoEnBD(Integer idDocumento, Usuario revisor) {
    Documento doc = docRepo.findById(idDocumento).orElseThrow();
    doc.setEstado(EstadoDocumento.RECHAZADO);
    doc.setRevisor(revisor);
    docRepo.save(doc);
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
  public List<DocumentoDTO> listarPorFamilia(Integer idFamilia) {
    if (!familiaRepo.existsById(idFamilia))
      throw new RecursoNoEncontradoException("No se encontró la familia: " + idFamilia);
    return docRepo.findByFamilia_IdFamilia(idFamilia).stream().map(mapper::toDTO).toList();
  }

  @Override
  public List<DocumentoDTO> listarPorOrganizacion(String cuitOrg) {
    if (!orgRepo.existsById(cuitOrg))
      throw new RecursoNoEncontradoException("No se encontró la organización: " + cuitOrg);
    return docRepo.findByOrganizacion_Cuit(cuitOrg).stream().map(mapper::toDTO).toList();
  }

  @Override
  public List<DocumentoDTO> listarPorUsuarioRevisor(Integer idUsuario) {
    if (!usuarioRepo.existsById(idUsuario))
      throw new RecursoNoEncontradoException("No se encontró el revisor: " + idUsuario);
    return docRepo.findByRevisor_Id(idUsuario).stream().map(mapper::toDTO).toList();
  }

  @Override
  public List<DocumentoDTO> listarPorSolicitudOrg(Integer idSolicitudOrg) {
    return docRepo.findBySolicitudOrganizacion_IdSolicitudOrg(idSolicitudOrg).stream().map(mapper::toDTO).toList();
  }

  private void validarArchivo(MultipartFile archivo, String nombreCampo) {
    if (archivo == null || archivo.isEmpty()) return;

    if (archivo.getSize() > MAX_SIZE_BYTES) {
      throw new ReglaDeNegocioException(
        "El archivo '" + nombreCampo + "' supera el máximo de 20MB (peso actual: " + (archivo.getSize() / (1024 * 1024)) + "MB)",
        HttpStatus.BAD_REQUEST);
    }

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
    return cloudinaryService.subirArchivo(archivo, "vivso_documentos");
  }

  private String extraerPublicId(String url) {
    try {
      String[] parts = url.split("/");
      String lastPart = parts[parts.length - 1]; // arreglado el error matemático
      String folder = parts[parts.length - 2];   // arreglado el error matemático
      String nombreSinExtension = lastPart.substring(0, lastPart.lastIndexOf("."));
      return folder + "/" + nombreSinExtension;
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public void borrarArchivoFisico(String url) {
    cloudinaryService.borrarArchivo(url);
  }
}