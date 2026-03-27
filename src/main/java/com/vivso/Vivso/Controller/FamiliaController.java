package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.FamiliaDTO;
import com.vivso.Vivso.Service.IFamiliaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/familia")
public class FamiliaController {

    @Autowired
    private IFamiliaService familiaService;

    @GetMapping("/listar")
    public ResponseEntity<List<FamiliaDTO>> listar(){
        List<FamiliaDTO> dtos = familiaService.getFamilias();
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @PostMapping("/crear")
    public ResponseEntity<FamiliaDTO> crear(@Valid @RequestBody FamiliaDTO familiaDTO){
        FamiliaDTO created = familiaService.saveFamilia(familiaDTO);
        return ResponseEntity.created(URI.create("/familia" + created.getIdFamilia())).body(created);
    }

    @PutMapping("/{idFamilia}")
    public ResponseEntity<FamiliaDTO> actualizar(@PathVariable Integer idFamilia,@Valid @RequestBody FamiliaDTO familiaDTO){
        FamiliaDTO familiaActualizada = familiaService.updateFamilia(idFamilia, familiaDTO);
        return new  ResponseEntity<>(familiaActualizada, HttpStatus.OK);
    }

    @DeleteMapping("/{idFamilia}")
    public ResponseEntity<String> eliminar (@PathVariable Integer idFamilia){
        familiaService.deleteFamilia(idFamilia);
        return ResponseEntity.ok("Familia eliminada");
    }

    @GetMapping("/{idFamilia}")
    public ResponseEntity<FamiliaDTO> get (@PathVariable Integer idFamilia){
        FamiliaDTO dto =  familiaService.findFamilia(idFamilia);
        return new  ResponseEntity<>(dto, HttpStatus.OK);
    }

}
