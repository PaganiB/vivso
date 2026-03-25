package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.OrganizacionDTO;
import com.vivso.Vivso.Service.IOrganizacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/organizacion")
public class OrganizacionController {

    @Autowired
    private IOrganizacionService orgService;

    @GetMapping("/listar")
    public ResponseEntity<List<OrganizacionDTO>> listar(){
        return ResponseEntity.ok(orgService.getOrganizacion());
    }

    @PostMapping
    public ResponseEntity<OrganizacionDTO> crear(@RequestBody OrganizacionDTO dto){
        OrganizacionDTO created = orgService.saveOrganizacion(dto);
        return ResponseEntity.created(URI.create("/controller" + created.getCuit())).body(created);
    }

    @PutMapping("/{cuit}")
    public ResponseEntity<OrganizacionDTO> actualizar(@PathVariable String cuit, @RequestBody OrganizacionDTO dto){
        return ResponseEntity.ok(orgService.updateOrganizacion(cuit, dto));
    }

    @DeleteMapping("/{cuit}")
    public void eliminar (@PathVariable String cuit){
        orgService.deleteOrganizacion(cuit);
    }

     @GetMapping("/buscarCuit/{cuit}")
    public ResponseEntity<OrganizacionDTO> buscarCuit(@PathVariable String cuit){
        return ResponseEntity.ok(orgService.buscarPorCuit(cuit));
    }

    @GetMapping("/buscarTipo/{tipo}")
    public  ResponseEntity<List<OrganizacionDTO>> buscarTipo(@PathVariable String tipo){
        return ResponseEntity.ok(orgService.buscarPorTipo(tipo));
    }

    @GetMapping("/buscarNombre/{nombre}")
    public ResponseEntity<List<OrganizacionDTO>> buscarNombre(@PathVariable String nombre){
        return ResponseEntity.ok(orgService.buscarPorNombre(nombre));
    }

}
