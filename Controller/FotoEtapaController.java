package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.FotoEtapaDTO;
import com.vivso.Vivso.Service.IFotoEtapaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/etapa/{idEtapa}/fotos")
@RequiredArgsConstructor
public class FotoEtapaController {

    private final IFotoEtapaService fotoEtapaService;

    @PostMapping
    public ResponseEntity<List<FotoEtapaDTO>> subirFotos(
            @PathVariable Integer idEtapa,
            @RequestParam("archivos") List<MultipartFile> archivos,
            Authentication auth) {

        return ResponseEntity.ok(fotoEtapaService.subirFotos(idEtapa, archivos, auth.getName()));
    }

    @GetMapping
    public ResponseEntity<List<FotoEtapaDTO>> listarFotos(
            @PathVariable Integer idEtapa,
            Authentication auth) {
        boolean esAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(fotoEtapaService.listarFotosPorEtapa(idEtapa, auth.getName(), esAdmin));
    }

    @DeleteMapping("/{idFoto}")
    public ResponseEntity<Void> eliminarFoto(
            @PathVariable Integer idEtapa, // Queda en la URL por consistencia REST
            @PathVariable Long idFoto,
            Authentication auth) {
        fotoEtapaService.eliminarFoto(idFoto, auth.getName());
        return ResponseEntity.noContent().build();
    }
}