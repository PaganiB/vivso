package com.vivso.Vivso.Service;

import com.cloudinary.Cloudinary;
import com.vivso.Vivso.DTO.FotoEtapaDTO;
import com.vivso.Vivso.Exception.ReglaDeNegocioException;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.*;
import com.vivso.Vivso.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FotoEtapaService implements IFotoEtapaService {

  private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
  @Autowired
  private IDocumentoService documentoService;
  @Autowired
  private IFotoEtapaRepository fotoRepo;
  @Autowired
  private IEtapaObraRepository etapaRepo;
  @Autowired
  private IUsuarioRepository usuarioRepo;
  @Autowired
  private IIntegranteRepository integranteRepo;
  @Autowired
  private VivsoMapper mapper;
  @Autowired
  private CloudinaryService cloudinaryService;

  private ReglaDeNegocioException prohibido(String msj) {
    return new ReglaDeNegocioException(msj, HttpStatus.FORBIDDEN);
  }

  private ReglaDeNegocioException conflicto(String msj) {
    return new ReglaDeNegocioException(msj, HttpStatus.CONFLICT);
  }

  private ReglaDeNegocioException noEncontrado(String msj) {
    return new ReglaDeNegocioException(msj, HttpStatus.NOT_FOUND);
  }

  @Override
  @Transactional
  public List<FotoEtapaDTO> subirFotos(Integer idEtapa, List<MultipartFile> archivos, String email) {
    EtapaObra etapa = etapaRepo.findById(idEtapa)
      .orElseThrow(() -> noEncontrado("Etapa no encontrada"));

    if (!esIntegranteDeLaOrganizacion(etapa.getSolicitud(), email)) {
      throw prohibido("Solo la organización asignada puede subir fotos.");
    }

    if (etapa.getEstado() != EstadoEtapa.EN_CURSO &&
      etapa.getEstado() != EstadoEtapa.OBSERVADA &&
      etapa.getEstado() != EstadoEtapa.PRORROGADA) {
      throw conflicto("No se pueden subir fotos a una etapa que no está en curso, observada o prorrogada.");
    }

    Usuario usuario = usuarioRepo.findByEmail(email).orElseThrow();
    List<FotoEtapa> nuevasFotos = new ArrayList<>();

    for (MultipartFile archivo : archivos) {
      if (archivo != null && !archivo.isEmpty()) {
        String urlCloudinary = cloudinaryService.subirArchivo(archivo, "fotosEtapas");

        FotoEtapa foto = FotoEtapa.builder()
          .etapa(etapa)
          .nombreArchivo(archivo.getOriginalFilename())
          .rutaArchivo(urlCloudinary)
          .fechaSubida(LocalDateTime.now(ZONA))
          .subidaPor(usuario)
          .build();

        nuevasFotos.add(foto);
      }
    }

    // Se guardan todas juntas y se mapean a DTO
    return fotoRepo.saveAll(nuevasFotos).stream()
      .map(mapper::toDTO)
      .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<FotoEtapaDTO> listarFotosPorEtapa(Integer idEtapa, String email, boolean esAdmin) {
    EtapaObra etapa = etapaRepo.findById(idEtapa)
      .orElseThrow(() -> noEncontrado("Etapa no encontrada"));

    if (!esAdmin && !esTecnicoAsignado(etapa.getSolicitud(), email) && !esIntegranteDeLaOrganizacion(etapa.getSolicitud(), email)) {
      throw prohibido("No tenés permiso para ver las fotos de esta etapa.");
    }

    // ACÁ ESTÁ LA CORRECCIÓN: findByEtapa_IdEtapaObra
    return fotoRepo.findByEtapa_IdEtapaObra(idEtapa).stream()
      .map(mapper::toDTO)
      .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void eliminarFoto(Long idFoto, String email) {
    FotoEtapa foto = fotoRepo.findById(idFoto)
      .orElseThrow(() -> noEncontrado("Foto no encontrada"));

    if (!esIntegranteDeLaOrganizacion(foto.getEtapa().getSolicitud(), email)) {
      throw prohibido("Solo la organización asignada puede eliminar fotos.");
    }
    if (foto.getEtapa().getEstado() != EstadoEtapa.EN_CURSO && foto.getEtapa().getEstado() != EstadoEtapa.OBSERVADA) {
      throw conflicto("No se pueden borrar fotos una vez enviada la etapa.");
    }

    // ==========================================
    // ELIMINACIÓN FÍSICA REAL
    // ==========================================
    documentoService.borrarArchivoFisico(foto.getRutaArchivo());

    fotoRepo.delete(foto);
  }

  // Métodos auxiliares idénticos a los de EtapaObraService
  private boolean esIntegranteDeLaOrganizacion(Solicitud solicitud, String email) {
    return integranteRepo.findByUsuario_Email(email)
      .map(i -> Objects.equals(i.getOrganizacion().getCuit(), solicitud.getCuitOrg().getCuit()))
      .orElse(false);
  }

  private boolean esTecnicoAsignado(Solicitud solicitud, String email) {
    Usuario tecnico = solicitud.getTecnicoAsignado();
    return tecnico != null && Objects.equals(tecnico.getEmail(), email);
  }
}