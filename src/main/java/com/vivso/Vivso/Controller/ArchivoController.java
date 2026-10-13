package com.vivso.Vivso.Controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/archivos")
public class ArchivoController {

    @Value("${vivso.upload.dir}")
    private String uploadDir;

    @GetMapping("/visitas/foto/{nombreArchivo:.+}")
    public ResponseEntity<Resource> obtenerFotoVisita(@PathVariable String nombreArchivo) {
        try {
            Path rutaFisica = Paths.get("uploadDir, visitas").resolve(nombreArchivo).normalize();
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

    @GetMapping("/documentos/{nombreArchivo:.+}")
    public ResponseEntity<Resource> obtenerDocumento(@PathVariable String nombreArchivo) {
        try {
            Path rutaFisica = Paths.get("uploadDir, documentos").resolve(nombreArchivo).normalize();
            Resource recurso = new UrlResource(rutaFisica.toUri());

            if (recurso.exists() && recurso.isReadable()) {
                // Detecta inteligentemente si es JPG, PNG o PDF
                String tipoMime = Files.probeContentType(rutaFisica);
                if (tipoMime == null) {
                    tipoMime = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(tipoMime))
                        .body(recurso);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}