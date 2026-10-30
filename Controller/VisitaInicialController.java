package com.vivso.Vivso.Controller;
import com.vivso.Vivso.DTO.VisitaHistorialDTO;
import com.vivso.Vivso.DTO.VisitaInicialDTO;
import com.vivso.Vivso.Service.IVisitaInicialService;
import org.springframework.core.io.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import tools.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/visita-inicial")
public class VisitaInicialController {

    @Autowired
    private IVisitaInicialService visitaService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<List<VisitaInicialDTO>> get(){
        return ResponseEntity.ok(visitaService.getVisitas());
    }


    //Metodo para recibir las fotos de la vivienda
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VisitaInicialDTO> save(
            @RequestParam("datos") String datosJson,
            MultipartHttpServletRequest request) {
        try {
            // Usamos tu ObjectMapper ya inyectado para deserializar el JSON de forma segura
            VisitaInicialDTO dto = objectMapper.readValue(datosJson, VisitaInicialDTO.class);

            // Extraemos todos los archivos asociados a la key "foto" que manda Axios
            List<MultipartFile> listaFotos = request.getFiles("fotos");

            // Llamamos a tu service con el DTO ya parseado y la lista de fotos
            VisitaInicialDTO nuevaVisita = visitaService.saveVisita(dto, listaFotos);

            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaVisita);
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar el JSON de la visita: " + e.getMessage());
        }
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

  @GetMapping("/historial-visitas")
  public ResponseEntity<List<VisitaHistorialDTO>> obtenerHistorial() {
    List<VisitaHistorialDTO> lista = visitaService.obtenerHistorialGeneral();
    return ResponseEntity.ok(lista);
  }

}