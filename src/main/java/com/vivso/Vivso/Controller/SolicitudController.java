package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.*;
import com.vivso.Vivso.Model.EstadoSolicitud;
import com.vivso.Vivso.Service.ISolicitudService;
import com.vivso.Vivso.Service.IUsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/solicitud")
public class SolicitudController {

  @Autowired
  private ISolicitudService solicitudService;
  @Autowired
  private IUsuarioService usuarioService;

  @PostMapping(value = "/familia", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<SolicitudDTO> registrarSolicitudFamilia(
          @Valid @RequestPart("datos") RegistroSolicitudDTO dto,
          MultipartHttpServletRequest request) {

    return ResponseEntity.status(HttpStatus.CREATED)
            .body(solicitudService.registrarSolicitudFamilia(
                    dto,
                    request.getMultiFileMap()
            ));
  }

  @PatchMapping("/{idSolicitud}/expediente")
  public ResponseEntity<SolicitudDTO> asignarExpediente(@PathVariable Integer idSolicitud,
                                                        @RequestParam String numExp) {
    return ResponseEntity.ok(solicitudService.asignarNumeroExpediente(idSolicitud, numExp));
  }

  @PutMapping("/{id}/aprobar")
  public ResponseEntity<SolicitudDTO> aprobarSolicitud(@PathVariable Integer id) {
    return ResponseEntity.ok(solicitudService.aprobarSolicitudVivienda(id));
  }

  @PutMapping("/{id}/rechazar")
  public ResponseEntity<Void> rechazarSolicitud(@PathVariable Integer id,
                                                @RequestBody Map<String, String> body) {
    solicitudService.rechazarSolicitudVivienda(id, body.get("motivo"));
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}/semiAprobar")
  public ResponseEntity<SolicitudDTO> semiAprobar(@PathVariable Integer id,
                                                  @RequestParam List<String> camposObservados,
                                                  @RequestParam String motivo) {
    return ResponseEntity.ok(solicitudService.semiAprobarSolicitud(id, camposObservados, motivo));
  }

  @PutMapping("/{id}/confirmar-correccion")
  public ResponseEntity<Void> confirmarCorreccion(@PathVariable Integer id) {
    solicitudService.confirmarCorreccion(id);
    return ResponseEntity.noContent().build();
  }

  // ── CRUD básico ───────────────────────────────────────────────────────
  @GetMapping
  public ResponseEntity<List<SolicitudDTO>> getSolicitud() {
    return ResponseEntity.ok(solicitudService.getSolicitudes());
  }

  //Metodo para obtener lista de solicitudes del tecnico
  @GetMapping("/tecnico/mis-expedientes")
  public ResponseEntity<List<ExpedienteWebDTO>> obtenerDashboardTecnicoWeb() {

    return ResponseEntity.ok(solicitudService.obtenerDashboardWebTecnico());
  }

  @GetMapping("/{id}/expediente")
  public ResponseEntity<ExpedienteDetalleDTO> obtenerExpediente(@PathVariable Integer id) {
    return ResponseEntity.ok(solicitudService.obtenerExpedienteDetalle(id));
  }

  // Metodo para obtener la lista de DTO a la app movil
  @GetMapping("/movil")
  public ResponseEntity<List<ExpedienteMovilDTO>> getSolicitudesMovil() {
    return ResponseEntity.ok(solicitudService.getSolicitudesParaMovil());
  }

  // Endpoint exclusivo para el rol INTEGRANTE en la app móvil
  @GetMapping("/mis-obras")
  public ResponseEntity<List<ExpedienteMovilDTO>> getObrasParaOngMovil() {
    return ResponseEntity.ok(solicitudService.getObrasAsignadasParaOng());
  }

  @GetMapping("/tecnicos-disponibles")
  public ResponseEntity<List<TecnicoDTO>> listarTecnicos() {
    return ResponseEntity.ok(usuarioService.listarTecnicosDisponibles());
  }

  @PutMapping("/{idSolicitud}/asignar-tecnico/{idTecnico}")
  public ResponseEntity<Void> asignarTecnico(
    @PathVariable Integer idSolicitud,
    @PathVariable Integer idTecnico) {

    solicitudService.asignarTecnico(idSolicitud, idTecnico);

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<SolicitudDTO> findSolicitud(@PathVariable Integer id) {
    return ResponseEntity.ok(solicitudService.findSolicitud(id));
  }

  @GetMapping("/estado/{estado}")
  public ResponseEntity<List<SolicitudDTO>> buscarEstado(@PathVariable String estado) {
    return ResponseEntity.ok(solicitudService.buscarPorEstado(EstadoSolicitud.from(estado)));
  }

  @GetMapping("/organizacion/{cuit}")
  public ResponseEntity<List<SolicitudDTO>> buscarOrganizacion(@PathVariable String cuit) {
    return ResponseEntity.ok(solicitudService.buscarPorOrganizacion(cuit));
  }

  @DeleteMapping("/{id}")
  public void deleteSolicitud(@PathVariable Integer id) {
    solicitudService.deleteSolicitud(id);
  }

}