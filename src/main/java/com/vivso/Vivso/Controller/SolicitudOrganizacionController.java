package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.*;
import com.vivso.Vivso.Service.ISolicitudOrganizacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/solicitud-organizacion")
public class SolicitudOrganizacionController {

    @Autowired
    private ISolicitudOrganizacionService solicitudOrgService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> registrarOrganizacion(
            @RequestPart("datos") RegistroOrganizacionDTO dto,
            MultipartHttpServletRequest request) {
        solicitudOrgService.registrarOrganizacion(dto, request.getMultiFileMap());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{cuit}/aprobar")
    public ResponseEntity<Void> aprobarOrganizacion(@PathVariable String cuit) {
        solicitudOrgService.aprobarOrganizacion(cuit);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{cuit}/rechazar")
    public ResponseEntity<Void> rechazarOrganizacion(@PathVariable String cuit,
                                                     @RequestParam String motivo) {
        solicitudOrgService.rechazarOrganizacion(cuit, motivo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{idSolicitud}/observaciones")
    public ResponseEntity<Void> marcarObservaciones(
            @PathVariable Integer idSolicitud,
            @RequestParam List<String> camposObservados,
            @RequestParam String motivo) {
        solicitudOrgService.marcarObservaciones(idSolicitud, camposObservados, motivo);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/token/{token}")
    public ResponseEntity<SolicitudOrganizacionDTO> obtenerPorToken(@PathVariable String token) {
        return ResponseEntity.ok(solicitudOrgService.obtenerSolicitudOrgPorToken(token));
    }

    @PatchMapping("/editar-token")
    public ResponseEntity<Void> editarConToken(
            @RequestParam String token,
            @RequestBody SolicitudOrganizacionPatchDTO dto) {
        solicitudOrgService.actualizarSolicitudOrgConToken(token, dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping(value = "/editar-token/documento/{idDoc}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> reemplazarDocumentoConToken(
            @RequestParam String token,
            @PathVariable Integer idDoc,
            @RequestParam("archivo") MultipartFile archivo) {
        solicitudOrgService.reemplazarDocumentoConToken(token, idDoc, archivo);
        return ResponseEntity.ok().build();
    }

}