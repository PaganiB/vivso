package com.vivso.Vivso.Controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
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
            Path rutaArchivo = Paths.get(uploadDir, "visitas").resolve(nombreArchivo).normalize();
            Resource recurso = new UrlResource(rutaArchivo.toUri());

            if (recurso.exists() && recurso.isReadable()) {

                // 1. Intentamos que Java detecte el formato automáticamente
                String contentType = Files.probeContentType(rutaArchivo);

                // 2. Si Java falla, lo detectamos nosotros manualmente por la extensión
                if (contentType == null) {
                    String extension = nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase();
                    switch (extension) {
                        case "png":
                            contentType = "image/png";
                            break;
                        case "jpg":
                        case "jpeg":
                            contentType = "image/jpeg";
                            break;
                        case "webp":
                            contentType = "image/webp";
                            break;
                        default:
                            contentType = "application/octet-stream"; // Descarga genérica si es un formato raro
                    }
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + recurso.getFilename() + "\"")
                        .body(recurso);
            } else {
                System.out.println("ERROR 404: Archivo no encontrado en la ruta física: " + rutaArchivo.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.out.println("ERROR INTERNO al buscar foto de visita: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/documentos/{nombreArchivo:.+}")
    public ResponseEntity<Resource> obtenerDocumento(@PathVariable String nombreArchivo) {
        try {
            Path rutaFisica = Paths.get(uploadDir, "documentos").resolve(nombreArchivo).normalize();
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