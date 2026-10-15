package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.VisitaObraDTO;
import com.vivso.Vivso.Service.IVisitaObraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/visita-obra")
@RequiredArgsConstructor
public class VisitaObraController {

    private final IVisitaObraService visitaObraService;

    @PostMapping
    public ResponseEntity<VisitaObraDTO> registrarVisita(@Valid @RequestBody VisitaObraDTO dto) {
        VisitaObraDTO created = visitaObraService.registrarVisita(dto);

        return ResponseEntity.created(URI.create("/visita-obra/" + created.getIdVisitaObra())).body(created);
    }

    @GetMapping("/vivienda/{idVivienda}")
    public ResponseEntity<List<VisitaObraDTO>> obtenerHistorialPorVivienda(@PathVariable Integer idVivienda) {
        List<VisitaObraDTO> historial = visitaObraService.obtenerHistorialPorVivienda(idVivienda);

        return ResponseEntity.ok(historial);
    }

    @PostMapping(value = "/movil/avance", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> registrarAvanceObra(
            @RequestParam("idSolicitud") Integer idSolicitud,
            @RequestParam("progreso") Integer progreso,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos) {

        visitaObraService.registrarAvanceDeObra(idSolicitud, progreso, observaciones, fotos);

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Avance de obra del " + progreso + "% registrado exitosamente en VIVSO.");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}