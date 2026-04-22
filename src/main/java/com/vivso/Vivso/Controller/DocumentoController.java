package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.DocumentoDTO;
import com.vivso.Vivso.Model.EstadoDocumento;
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

    // 2. REVISAR DOCUMENTO (Aprobar o Rechazar)
    // Usamos @PatchMapping porque solo estamos actualizando una parte del documento (el estado)
    @PatchMapping("/{idDoc}/revisar")
    public ResponseEntity<DocumentoDTO> revisarDocumento(
            @PathVariable Integer idDoc,
            @RequestParam("idRevisor") Integer idRevisor,
            @RequestParam("nuevoEstado") EstadoDocumento nuevoEstado,
            @RequestParam(value = "motivo", required = false) String motivo) {

        DocumentoDTO actualizado = documentoService.revisar(idDoc, idRevisor, nuevoEstado, motivo);
        return ResponseEntity.ok(actualizado);
    }

    // 3. LISTAR POR FAMILIA
    @GetMapping("/familia/{idFamilia}")
    public ResponseEntity<List<DocumentoDTO>> listarPorFamilia(@PathVariable Integer idFamilia) {
        return ResponseEntity.ok(documentoService.listarPorFamilia(idFamilia));
    }

    // 4. LISTAR POR ORGANIZACIÓN
    @GetMapping("/organizacion/{cuitOrg}")
    public ResponseEntity<List<DocumentoDTO>> listarPorOrganizacion(@PathVariable String cuitOrg) {
        return ResponseEntity.ok(documentoService.listarPorOrganizacion(cuitOrg));
    }

    // 5. LISTAR POR USUARIO REVISOR
    @GetMapping("/revisor/{idUsuario}")
    public ResponseEntity<List<DocumentoDTO>> listarPorUsuarioRevisor(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(documentoService.listarPorUsuarioRevisor(idUsuario));
    }

    // 6. ELIMINAR DOCUMENTO
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Integer id) {
        documentoService.eliminar(id);
        return ResponseEntity.ok("Documento eliminado correctamente");
    }
}
