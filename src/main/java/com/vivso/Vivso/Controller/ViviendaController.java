package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.ViviendaDTO;
import com.vivso.Vivso.Model.EstadoVivienda;
import com.vivso.Vivso.Service.IViviendaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/vivienda")
public class ViviendaController {

    @Autowired
    private IViviendaService viviendaService;

    // POST /vivienda -> CREAR
    @PostMapping
    public ResponseEntity<ViviendaDTO> crear(@Valid @RequestBody ViviendaDTO dto) {
        ViviendaDTO created = viviendaService.guardar(dto);
        return ResponseEntity.created(URI.create("/vivienda/" + created.getIdVivienda())).body(created);
    }

    // GET /vivienda -> LISTAR TODAS
    @GetMapping
    public ResponseEntity<List<ViviendaDTO>> listarTodas() {
        return ResponseEntity.ok(viviendaService.listarTodas());
    }

    // GET /vivienda/{idVivienda}
    @GetMapping("/{idVivienda}")
    public ResponseEntity<ViviendaDTO> buscarPorId(@PathVariable Integer idVivienda) {
        return ResponseEntity.ok(viviendaService.buscarPorId(idVivienda));
    }

    // DELETE /vivienda/{numExp} -> ELIMINAR
    @DeleteMapping("/{idVivienda}")
    public ResponseEntity<String> eliminar(@PathVariable Integer idVivienda) {
        viviendaService.eliminar(idVivienda);
        return ResponseEntity.ok("Vivienda con ID " + idVivienda + " eliminada con éxito.");
    }

    // Estos mantienen un sub-path porque filtran por criterios específicos

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ViviendaDTO>> buscarPorEstado(@PathVariable EstadoVivienda estado) {
        return ResponseEntity.ok(viviendaService.buscarPorEstado(estado));
    }

    @GetMapping("/familia/{idFamilia}")
    public ResponseEntity<ViviendaDTO> buscarPorFamilia(@PathVariable Integer idFamilia) {
        return viviendaService.buscarPorFamilia(idFamilia)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{idVivienda}")
    public ResponseEntity<ViviendaDTO> actualizar(@PathVariable Integer idVivienda, @Valid @RequestBody ViviendaDTO dto) {
        ViviendaDTO actualizado = viviendaService.actualizar(idVivienda, dto);
        return ResponseEntity.ok(actualizado);
    }
}
