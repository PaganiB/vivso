package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.VisitaHistorialDTO;
import com.vivso.Vivso.Service.HistorialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/historial-visitas")
public class HistorialController {

    @Autowired
    private HistorialService historialService;

    @GetMapping
    public ResponseEntity<List<VisitaHistorialDTO>> obtenerHistorial() {
        List<VisitaHistorialDTO> lista = historialService.obtenerHistorialGeneral();
        return ResponseEntity.ok(lista);
    }
}