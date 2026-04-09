package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.SolicitudDTO;
import com.vivso.Vivso.Model.EstadoSolicitud;
import com.vivso.Vivso.Service.ISolicitudService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
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

    @PostMapping
    public ResponseEntity<SolicitudDTO> saveSolicitud(@Valid @RequestBody SolicitudDTO solicitudDto){
        SolicitudDTO created =  solicitudService.saveSolicitud(solicitudDto);
        return ResponseEntity.created(URI.create("/solicitud" + created.getIdSolicitud())).body(created);
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
