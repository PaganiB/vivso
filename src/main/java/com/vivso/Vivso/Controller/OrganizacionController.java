package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.OrganizacionDTO;
import com.vivso.Vivso.Service.IOrganizacionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/organizacion")
public class OrganizacionController {

    @Autowired
    private IOrganizacionService orgService;

    @GetMapping
    public ResponseEntity<List<OrganizacionDTO>> listar(){
        return ResponseEntity.ok(orgService.getOrganizacion());
    }

    @PostMapping
    public ResponseEntity<OrganizacionDTO> crear(@Valid @RequestBody OrganizacionDTO dto){
        OrganizacionDTO created = orgService.saveOrganizacion(dto);
        return ResponseEntity.created(URI.create("/controller" + created.getCuit())).body(created);
    }

    @PutMapping("/{cuit}")
    public ResponseEntity<OrganizacionDTO> actualizar(@PathVariable String cuit, @Valid @RequestBody OrganizacionDTO dto){
        OrganizacionDTO dtoActualizado = orgService.updateOrganizacion(cuit, dto);
        return new ResponseEntity<>(dtoActualizado, HttpStatus.OK);
    }

    @DeleteMapping("/{cuit}")
    public String eliminar (@PathVariable String cuit){
        orgService.deleteOrganizacion(cuit);
        return "Organizacion eliminada";
    }

     @GetMapping("/buscarCuit/{cuit}")
    public ResponseEntity<OrganizacionDTO> buscarCuit(@PathVariable String cuit){
        OrganizacionDTO org = orgService.buscarPorCuit(cuit);
        return new ResponseEntity<> (org, HttpStatus.OK);
    }

    @GetMapping("/buscarTipo/{tipo}")
    public  ResponseEntity<List<OrganizacionDTO>> buscarTipo(@PathVariable String tipo){
        List<OrganizacionDTO> dtos = orgService.buscarPorTipo(tipo);
        return new  ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping("/buscarNombre/{nombre}")
    public ResponseEntity<List<OrganizacionDTO>> buscarNombre(@PathVariable String nombre){
        List<OrganizacionDTO> dtos = orgService.buscarPorNombre(nombre);
        return new  ResponseEntity<>(dtos, HttpStatus.OK);
    }

}
