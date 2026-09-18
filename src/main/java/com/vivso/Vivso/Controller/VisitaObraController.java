package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.VisitaObraDTO;
import com.vivso.Vivso.Service.IVisitaObraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/visita-obra")
@RequiredArgsConstructor
public class VisitaObraController {

    private final IVisitaObraService visitaService;

    @PostMapping
    public ResponseEntity<VisitaObraDTO> registrarVisita(@Valid @RequestBody VisitaObraDTO dto) {
        VisitaObraDTO created = visitaService.registrarVisita(dto);

        return ResponseEntity.created(URI.create("/visita-obra/" + created.getIdVisitaObra())).body(created);
    }

    @GetMapping("/vivienda/{idVivienda}")
    public ResponseEntity<List<VisitaObraDTO>> obtenerHistorialPorVivienda(@PathVariable Integer idVivienda) {
        List<VisitaObraDTO> historial = visitaService.obtenerHistorialPorVivienda(idVivienda);

        return ResponseEntity.ok(historial);
    }
}