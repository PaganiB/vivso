package com.vivso.Vivso.Controller;

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
    /*@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SolicitudDTO> crearSolicitud(
            @RequestPart("datos") SolicitudCompletaDTO dto,

            @RequestPart(required = false) MultipartFile notaSolicitud,
            @RequestPart(required = false) MultipartFile actaAsamblea,
            @RequestPart(required = false) MultipartFile constanciaCuentaBancaria,
            @RequestPart(required = false) MultipartFile altaAfip,
            @RequestPart(required = false) MultipartFile dniPresidente,
            @RequestPart(required = false) MultipartFile dniTesorero,

            @RequestPart(required = false) MultipartFile certificadoResidencia,
            @RequestPart(required = false) MultipartFile certificadoDiscapacidad,
            @RequestPart(required = false) MultipartFile escrituraPropiedad,
            @RequestPart(required = false) MultipartFile declaracionJurada,
            @RequestPart(required = false) MultipartFile actaCompromiso,

            @RequestPart(required = false) List<MultipartFile> dniFamiliares,
            @RequestPart(required = false) List<MultipartFile> fotosTerreno
    ) {

        dto.setNotaSolicitud(notaSolicitud);
        dto.setActaAsamblea(actaAsamblea);
        dto.setConstanciaCuentaBancaria(constanciaCuentaBancaria);
        dto.setAltaAfip(altaAfip);
        dto.setDniPresidente(dniPresidente);
        dto.setDniTesorero(dniTesorero);

        dto.setCertificadoResidencia(certificadoResidencia);
        dto.setCertificadoDiscapacidad(certificadoDiscapacidad);
        dto.setEscrituraPropiedad(escrituraPropiedad);
        dto.setDeclaracionJurada(declaracionJurada);
        dto.setActaCompromiso(actaCompromiso);

        dto.setDniFamiliares(dniFamiliares);
        dto.setFotosTerreno(fotosTerreno);

        SolicitudDTO solicitudCreada =
                solicitudService.crearSolicitudCompleta(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(solicitudCreada);
    }*/

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
