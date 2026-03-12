package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.UsuarioRegistroDTO;
import com.vivso.Vivso.DTO.UsuarioRespuestaDTO;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/crear")
    public ResponseEntity<?> crear(@Valid @RequestBody UsuarioRegistroDTO registroDto) {
        try {
            // 1. Validaciones de lógica de negocio (duplicados)
            if (usuarioService.existePorEmail(registroDto.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("El email ya está registrado.");
            }

            if (usuarioService.existePorUsername(registroDto.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("El nombre de usuario ya existe.");
            }

            // 2. Mapeo de DTO a Entidad y guardado
            // Aquí el Service debería hashear la password antes de guardar
            Usuario nuevoUsuario = usuarioService.registrarNuevoUsuario(registroDto);

            // 3. Retornar solo los datos permitidos
            UsuarioRespuestaDTO respuesta = new UsuarioRespuestaDTO(
                    nuevoUsuario.getId_usuario(),
                    nuevoUsuario.getUsername(),
                    nuevoUsuario.getEmail(),
                    nuevoUsuario.getRol()
            );

            return new ResponseEntity<>(respuesta, HttpStatus.CREATED);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado en el servidor.");
        }
    }

    @GetMapping("/listar")
    public ResponseEntity<List<UsuarioRespuestaDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable("id") int id) {
        try {
            return ResponseEntity.ok(usuarioService.buscarPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivar(@PathVariable Integer id) {
        try {
            usuarioService.desactivar(id);
            return ResponseEntity.ok("Usuario desactivado");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}