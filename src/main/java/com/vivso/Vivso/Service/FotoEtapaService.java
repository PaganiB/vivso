package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.FotoEtapaDTO;
import com.vivso.Vivso.Exception.ReglaDeNegocioException;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.*;
import com.vivso.Vivso.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private IDocumentoService documentoService;

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");

    private final IFotoEtapaRepository fotoRepo;
    private final IEtapaObraRepository etapaRepo;
    private final IUsuarioRepository usuarioRepo;
    private final IIntegranteRepository integranteRepo;
    private final VivsoMapper mapper;

    // Inyectá acá tu servicio de archivos que ya usabas en VisitaInicial o Documentos
    // private final ArchivoService archivoService;

    private ReglaDeNegocioException prohibido(String msj) { return new ReglaDeNegocioException(msj, HttpStatus.FORBIDDEN); }
    private ReglaDeNegocioException conflicto(String msj) { return new ReglaDeNegocioException(msj, HttpStatus.CONFLICT); }
    private ReglaDeNegocioException noEncontrado(String msj) { return new ReglaDeNegocioException(msj, HttpStatus.NOT_FOUND); }

    @Override
    @Transactional
    public List<FotoEtapaDTO> subirFotos(Integer idEtapa, List<MultipartFile> archivos, String email) {
        EtapaObra etapa = etapaRepo.findById(idEtapa)
                .orElseThrow(() -> noEncontrado("Etapa no encontrada"));

        if (!esIntegranteDeLaOrganizacion(etapa.getSolicitud(), email)) {
            throw prohibido("Solo la organización asignada puede subir fotos.");
        }

        if (etapa.getEstado() != EstadoEtapa.EN_CURSO && etapa.getEstado() != EstadoEtapa.OBSERVADA) {
            throw conflicto("No se pueden subir fotos a una etapa que no está en curso o en corrección.");
        }

        Usuario usuario = usuarioRepo.findByEmail(email).orElseThrow();
        List<FotoEtapa> nuevasFotos = new ArrayList<>();

        for (MultipartFile archivo : archivos) {
            if (archivo != null && !archivo.isEmpty()) {
                String rutaReal = documentoService.guardarArchivoFisico(archivo);

                FotoEtapa foto = FotoEtapa.builder()
                        .etapa(etapa)
                        .nombreArchivo(archivo.getOriginalFilename())
                        .rutaArchivo(rutaReal)
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

        boolean esTecnico = esTecnicoAsignado(etapa.getSolicitud(), email);
        boolean esOng = esIntegranteDeLaOrganizacion(etapa.getSolicitud(), email);

        if (!esAdmin && !esTecnico && !esOng) {
            throw prohibido("No tenés permiso para ver las fotos de esta etapa.");
        }
        return fotoRepo.findByEtapa_IdEtapaObra(idEtapa).stream()
                .filter(foto -> {
                    // El Admin y el Técnico asignado tienen acceso total
                    if (esAdmin || esTecnico) return true;

                    // El integrante de la ONG solo ve las fotos donde su usuario sea el autor
                    return foto.getSubidaPor() != null && foto.getSubidaPor().getEmail().equals(email);
                })
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