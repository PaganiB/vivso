package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.IntegranteDTO;
import com.vivso.Vivso.Service.IIntegranteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/integrante")
public class IntegranteController {

    @Autowired
    IIntegranteService integranteService;

    @GetMapping
    public ResponseEntity<List<IntegranteDTO>> listarIntegrante(){
        return ResponseEntity.ok(integranteService.getIntegrantes());
    }

    @PutMapping ("/{idIntegrante}")
    public ResponseEntity<IntegranteDTO> editarIntegrante(@PathVariable Integer idIntegrante, @Valid @RequestBody IntegranteDTO integrante){
        return ResponseEntity.ok(integranteService.updateIntegrantes(idIntegrante, integrante));
    }

    @DeleteMapping("/{idIntegrante}")
    public void eliminarIntegrante(@PathVariable Integer idIntegrante){
        integranteService.deleteIntegrantes(idIntegrante);
    }

    @GetMapping("/organizacion/{cuit}")
    public ResponseEntity<List<IntegranteDTO>> listarPorOrganizacion(@PathVariable String cuit){
        return ResponseEntity.ok(integranteService.listarPorOrganizacion(cuit));
    }

    @GetMapping("/activos")
    public ResponseEntity<List<IntegranteDTO>> listarActivos(){
        return ResponseEntity.ok(integranteService.buscarActivos());
    }

    @PostMapping("/{idIntegrante}/reenviar-credenciales")
    public ResponseEntity<String> reenviarCredenciales(@PathVariable Integer idIntegrante) {
        integranteService.reenviarCredenciales(idIntegrante);
        return ResponseEntity.ok("Se envió una nueva contraseña temporal al correo del integrante.");
    }
}
