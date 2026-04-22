package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.PasswordUpdateDTO;
import com.vivso.Vivso.DTO.UsuarioRegistroDTO;
import com.vivso.Vivso.DTO.UsuarioRespuestaDTO;
import com.vivso.Vivso.Mapper.Mapper;
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
    public ResponseEntity<UsuarioRespuestaDTO> crear(@Valid @RequestBody UsuarioRegistroDTO registroDto) {
        Usuario nuevoUsuario = usuarioService.registrarNuevoUsuario(registroDto);
        return new ResponseEntity<>(Mapper.toRespuestaDTO(nuevoUsuario), HttpStatus.CREATED);
    }

    //METODO GET PARA VER TODOS LOS USUARIOS
    @GetMapping("/listar")
    public ResponseEntity<List<UsuarioRespuestaDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioRespuestaDTO> buscar(@PathVariable("id") int id) {
            return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<String> desactivar(@PathVariable Integer id) {
            usuarioService.desactivar(id);
            return ResponseEntity.ok("Usuario desactivado");
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<String> activar(@PathVariable Integer id) {
        usuarioService.activar(id);
        return ResponseEntity.ok("Usuario activado");
    }

    @PatchMapping("/{id}/cambiar-password")
    public ResponseEntity<String> cambiarPassword(@PathVariable Integer id, @Valid @RequestBody PasswordUpdateDTO dto) {
        usuarioService.actualizarPassword(id, dto);
        return ResponseEntity.ok("Contraseña actualizada con éxito en el sistema.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioRespuestaDTO> actualizar(@PathVariable Integer id, @Valid @RequestBody UsuarioRegistroDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizar(id, dto));
    }
}