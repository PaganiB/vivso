package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.RegistroFamiliaDTO;
import com.vivso.Vivso.DTO.RegistroOrganizacionDTO;
import com.vivso.Vivso.DTO.SolicitudCompletaDTO;
import com.vivso.Vivso.DTO.SolicitudDTO;
import com.vivso.Vivso.Model.EstadoSolicitud;
import com.vivso.Vivso.Service.ISolicitudService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;


import java.util.List;

@RestController
@RequestMapping("/solicitud")
public class SolicitudController {

    @Autowired
    private ISolicitudService solicitudService;

    @GetMapping
    public ResponseEntity<List<SolicitudDTO>> getSolicitud(){
        return ResponseEntity.ok(solicitudService.getSolicitudes());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SolicitudDTO> crearSolicitud(
            @RequestPart("datos") SolicitudCompletaDTO dto,
            MultipartHttpServletRequest request
    ) {

        // request.getMultiFileMap() extrae TODOS los archivos físicos que mandó React
        // y se los pasamos al Service en un solo bloque.
        SolicitudDTO solicitudCreada = solicitudService.crearSolicitudCompleta(dto, request.getMultiFileMap());

        return ResponseEntity.status(HttpStatus.CREATED).body(solicitudCreada);
    }

    // Formulario 1: registrar una organización en el programa
    @PostMapping(value = "/organizacion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> registrarOrganizacion(@RequestPart("datos") RegistroOrganizacionDTO dto, MultipartHttpServletRequest request) {
        solicitudService.registrarOrganizacion(dto, request.getMultiFileMap());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Formulario 2: presentar una solicitud de vivienda para una familia
    @PostMapping(value = "/familia", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SolicitudDTO> registrarSolicitudFamilia(@RequestPart("datos") RegistroFamiliaDTO dto, MultipartHttpServletRequest request) {
        SolicitudDTO resultado = solicitudService.registrarSolicitudFamilia(dto, request.getMultiFileMap());
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SolicitudDTO> updateSolicitud(@PathVariable Integer id, @Valid @RequestBody SolicitudDTO solicitudDto){
        return  ResponseEntity.ok(solicitudService.updateSolicitud(id,solicitudDto));
    }

    @DeleteMapping("/{id}")
    public void deleteSolicitud(@PathVariable Integer id){
        solicitudService.deleteSolicitud(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudDTO> findSolicitud(@PathVariable Integer id){
        return ResponseEntity.ok(solicitudService.findSolicitud(id));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<SolicitudDTO>> buscarEstado(@PathVariable String estado){
        EstadoSolicitud estadoEnum = EstadoSolicitud.from(estado);
        return ResponseEntity.ok(solicitudService.buscarPorEstado(estadoEnum));
    }

    @GetMapping("/organizacion/{cuit}")
    public ResponseEntity<List<SolicitudDTO>> buscarOrganizacion(@PathVariable String cuit){
        return ResponseEntity.ok(solicitudService.buscarPorOrganizacion(cuit));
    }

}
