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
        return ResponseEntity.created(URI.create("/vivienda" + created.getNumExp())).build();
    }

    // GET /vivienda -> LISTAR TODAS
    @GetMapping
    public ResponseEntity<List<ViviendaDTO>> listarTodas() {
        return ResponseEntity.ok(viviendaService.listarTodas());
    }

    // GET /vivienda/{numExp} -> BUSCAR POR ID
    @GetMapping("/{numExp}")
    public ResponseEntity<ViviendaDTO> buscarPorExpediente(@PathVariable String numExp) {
        return ResponseEntity.ok(viviendaService.buscarPorExpediente(numExp));
    }

    // DELETE /vivienda/{numExp} -> ELIMINAR
    @DeleteMapping("/{numExp}")
    public ResponseEntity<String> eliminar(@PathVariable String numExp) {
        viviendaService.eliminar(numExp);
        return ResponseEntity.ok("Vivienda con expediente " + numExp + " eliminada con éxito.");
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

    @PutMapping("/{numExp}")
    public ResponseEntity<ViviendaDTO> actualizar(@PathVariable String numExp, @Valid @RequestBody ViviendaDTO dto) {
        ViviendaDTO actualizado = viviendaService.actualizar(numExp, dto);
        return ResponseEntity.ok(actualizado);
    }
}
