package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.DocumentoDTO;
import com.vivso.Vivso.Model.TipoDocumento;
import com.vivso.Vivso.Service.IDocumentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/documento")
public class DocumentoController {

    @Autowired
    private IDocumentoService documentoService;

    // 1. SUBIR DOCUMENTO (Requiere form-data en Postman)
    @PostMapping()
    public ResponseEntity<DocumentoDTO> subirDocumento(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipo") TipoDocumento tipo,
            @RequestParam(value = "idFamilia", required = false) Integer idFamilia,
            @RequestParam(value = "cuitOrg", required = false) String cuitOrg) {

        DocumentoDTO created = documentoService.subirDocumento(archivo, tipo, idFamilia, cuitOrg);

        // Devolvemos 201 CREATED con la URI correcta
        return ResponseEntity.created(URI.create("/documento/" + created.getIdDocumento())).body(created);
    }

    @PatchMapping("/{idDoc}/marcar-corregir")
    public ResponseEntity<DocumentoDTO> marcarParaCorregir(
            @PathVariable Integer idDoc,
            @RequestParam String motivo) {

        DocumentoDTO actualizado = documentoService.marcarParaCorregir(idDoc, motivo);
        return ResponseEntity.ok(actualizado);
    }

    // 3. Remplazar documento rechazado por operador
    @PatchMapping("/{id}/reemplazar")
    public ResponseEntity<DocumentoDTO> reemplazarDocumento(
            @PathVariable Integer id,
            @RequestParam("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(documentoService.reemplazar(id, archivo));
    }

    // 4. LISTAR POR FAMILIA
    @GetMapping("/familia/{idFamilia}")
    public ResponseEntity<List<DocumentoDTO>> listarPorFamilia(@PathVariable Integer idFamilia) {
        return ResponseEntity.ok(documentoService.listarPorFamilia(idFamilia));
    }

    // 5. LISTAR POR ORGANIZACIÓN
    @GetMapping("/organizacion/{cuitOrg}")
    public ResponseEntity<List<DocumentoDTO>> listarPorOrganizacion(@PathVariable String cuitOrg) {
        return ResponseEntity.ok(documentoService.listarPorOrganizacion(cuitOrg));
    }

    // 6. LISTAR POR USUARIO REVISOR
    @GetMapping("/revisor/{idUsuario}")
    public ResponseEntity<List<DocumentoDTO>> listarPorUsuarioRevisor(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(documentoService.listarPorUsuarioRevisor(idUsuario));
    }

    // 7. ELIMINAR DOCUMENTO
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Integer id) {
        documentoService.eliminar(id);
        return ResponseEntity.ok("Documento eliminado correctamente");
    }
}
