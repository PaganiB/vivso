package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.ViviendaDTO;
import com.vivso.Vivso.DTO.ViviendaRegistroDTO;
import com.vivso.Vivso.Service.IViviendaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vivienda")
public class ViviendaController {

    @Autowired
    private IViviendaService viviendaService;

    @GetMapping
    public ResponseEntity<List<ViviendaDTO>> listarTodas() {
        return ResponseEntity.ok(viviendaService.listarTodas());
    }

    @GetMapping("/{idVivienda}")
    public ResponseEntity<ViviendaDTO> buscarPorId(@PathVariable Integer idVivienda) {
        return ResponseEntity.ok(viviendaService.buscarPorId(idVivienda));
    }

    @DeleteMapping("/{idVivienda}")
    public ResponseEntity<String> eliminar(@PathVariable Integer idVivienda) {
        viviendaService.eliminar(idVivienda);
        return ResponseEntity.ok("Vivienda con ID " + idVivienda + " eliminada con éxito.");
    }

    @GetMapping("/familia/{idFamilia}")
    public ResponseEntity<ViviendaDTO> buscarPorFamilia(@PathVariable Integer idFamilia) {
        return viviendaService.buscarPorFamilia(idFamilia)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{idVivienda}")
    public ResponseEntity<ViviendaDTO> actualizar(
            @PathVariable Integer idVivienda,
            @Valid @RequestBody ViviendaRegistroDTO dto) {

        ViviendaDTO actualizado = viviendaService.actualizar(idVivienda, dto);
        return ResponseEntity.ok(actualizado);
    }
}