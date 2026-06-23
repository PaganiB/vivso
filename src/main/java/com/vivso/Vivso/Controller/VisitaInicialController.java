package com.vivso.Vivso.Controller;
import com.vivso.Vivso.DTO.VisitaInicialDTO;
import com.vivso.Vivso.Service.IVisitaInicialService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/visita")
public class VisitaInicialController {

    @Autowired
    private IVisitaInicialService visitaService;

    @GetMapping
    public ResponseEntity<List<VisitaInicialDTO>> get(){
        return ResponseEntity.ok(visitaService.getVisitas());
    }

    @PostMapping
    public ResponseEntity<VisitaInicialDTO> save(@Valid @RequestBody VisitaInicialDTO visitaInicialDTO){
        VisitaInicialDTO created = visitaService.saveVisita(visitaInicialDTO);
        return  ResponseEntity.created(URI.create("/visita/" +created.getIdVisita())).body(created);
    }

    @PutMapping("/{idVisita}")
    public ResponseEntity<VisitaInicialDTO> update(@PathVariable Integer idVisita, @Valid @RequestBody VisitaInicialDTO visitaInicialDTO){
        return ResponseEntity.ok(visitaService.updateVisita(idVisita, visitaInicialDTO));
    }

    @DeleteMapping("/{idVisita}")
    public void delete(@PathVariable Integer idVisita){
        visitaService.deleteVisita(idVisita);
    }

    @GetMapping("/{idVisita}")
    public ResponseEntity<VisitaInicialDTO> findVisita(@PathVariable Integer idVisita){
        return ResponseEntity.ok(visitaService.FindVisita(idVisita));
    }

    @GetMapping("/tecnico/{idVisita}")
    public ResponseEntity<List<VisitaInicialDTO>> findVisitasTecnico(@PathVariable Integer idVisita){
        return ResponseEntity.ok(visitaService.FindVisitaPorTecnico(idVisita));
    }
}