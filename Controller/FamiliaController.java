package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.FamiliaDTO;
import com.vivso.Vivso.Service.IFamiliaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/familia")
public class FamiliaController {

    @Autowired
    private IFamiliaService familiaService;

    @GetMapping
    public ResponseEntity<List<FamiliaDTO>> listar(){
        return ResponseEntity.ok(familiaService.getFamilias());
    }

    @PostMapping
    public ResponseEntity<FamiliaDTO> crear(@Valid @RequestBody FamiliaDTO familiaDTO){
        FamiliaDTO created = familiaService.saveFamilia(familiaDTO);
        return ResponseEntity.created(URI.create("/familia/" + created.getIdFamilia())).body(created);
    }

    @PutMapping("/{idFamilia}")
    public ResponseEntity<FamiliaDTO> actualizar(@PathVariable Integer idFamilia, @Valid @RequestBody FamiliaDTO familiaDTO){
        return ResponseEntity.ok(familiaService.updateFamilia(idFamilia, familiaDTO));
    }

    @DeleteMapping("/{idFamilia}")
    public void eliminar (@PathVariable Integer idFamilia){
        familiaService.deleteFamilia(idFamilia);
    }

    @GetMapping("/{idFamilia}")
    public ResponseEntity<FamiliaDTO> buscar(@PathVariable Integer idFamilia){
        return ResponseEntity.ok(familiaService.findFamilia(idFamilia));
    }

}
