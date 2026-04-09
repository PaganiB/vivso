package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.FamiliarDTO;
import com.vivso.Vivso.DTO.OrganizacionDTO;
import com.vivso.Vivso.Service.FamiliarService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/familiar")
public class FamiliarController {

    @Autowired
    private FamiliarService familiarService;

    // MOSTRAR TODOS LOS INTEGRANTES FAMILIARES
    @GetMapping("/lista")
    public ResponseEntity<List<FamiliarDTO>> listarTodos() {
        return ResponseEntity.ok(familiarService.listarTodos());
    }

    //BUSCAR FAMILIAR POR ID
    @GetMapping("/{id}")
    public ResponseEntity<FamiliarDTO> buscarPorId(@PathVariable Integer id) {
        FamiliarDTO dto = familiarService.buscarPorId(id);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    //BUSCAR FAMILIAR POR DNI
    @GetMapping("/dni/{dni}")
    public ResponseEntity<FamiliarDTO> buscarPorDni(@PathVariable String dni) {
        return ResponseEntity.ok(familiarService.buscarPorDni(dni));
    }

    //CREAR FAMILIAR
    @PostMapping("/crear")
    public ResponseEntity<FamiliarDTO> crear(@Valid @RequestBody FamiliarDTO dto) {
        FamiliarDTO created = familiarService.guardar(dto);
        return ResponseEntity.created(URI.create("/familiar/crear" + created.getIdIntegranteFamilia())).body(created);
    }

    // BUSCAR INTEGRANTES DE UNA FAMILIA
    @GetMapping("/familia/{id_familia}")
    public ResponseEntity<List<FamiliarDTO>> buscarPorFamilia(@PathVariable Integer id_familia) {
        List<FamiliarDTO> lista = familiarService.buscarPorFamilia(id_familia);
        return ResponseEntity.ok(lista);
    }

    // ELIMINAR POR ID UN FAMILIAR
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Integer id) {
        familiarService.eliminar(id);
        return ResponseEntity.ok("Integrante eliminado correctamente");
    }

    //EDITAR UN FAMILIAR POR ID
    @PutMapping("/editar/{id}")
    public ResponseEntity<FamiliarDTO> actualizar(@PathVariable Integer id, @Valid @RequestBody FamiliarDTO dto) {
        FamiliarDTO actualizado = familiarService.actualizar(id, dto);
        return ResponseEntity.ok(actualizado);
    }
}
