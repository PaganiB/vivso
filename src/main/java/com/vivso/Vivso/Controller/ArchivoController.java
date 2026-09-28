package com.vivso.Vivso.Controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/archivos")
public class ArchivoController {

    @GetMapping("/visitas/foto/{nombreArchivo:.+}")
    public ResponseEntity<Resource> obtenerFotoVisita(@PathVariable String nombreArchivo) {
        try {
            Path rutaFisica = Paths.get("uploads/visitas").resolve(nombreArchivo).normalize();
            Resource recurso = new UrlResource(rutaFisica.toUri());

            if (recurso.exists() && recurso.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(recurso);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // El día de mañana, si necesitas servir PDFs de manera segura, agregas otro méttodo acá:
}