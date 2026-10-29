package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.PasswordUpdateDTO;
import com.vivso.Vivso.DTO.UsuarioRegistroDTO;
import com.vivso.Vivso.DTO.UsuarioRespuestaDTO;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

  @Autowired
  private UsuarioService usuarioService;
  @Autowired
  private VivsoMapper mapper;

  @PostMapping("/crear")
  public ResponseEntity<UsuarioRespuestaDTO> crear(@Valid @RequestBody UsuarioRegistroDTO registroDto) {
    Usuario nuevoUsuario = usuarioService.registrarNuevoUsuario(registroDto);
    return new ResponseEntity<>(mapper.toRespuestaDTO(nuevoUsuario), HttpStatus.CREATED);
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

  @PutMapping("/{id}")
  public ResponseEntity<UsuarioRespuestaDTO> actualizar(@PathVariable Integer id, @Valid @RequestBody UsuarioRegistroDTO dto) {
    return ResponseEntity.ok(usuarioService.actualizar(id, dto));
  }

  @PostMapping("/{id}/reenviar-credenciales")
  public ResponseEntity<String> reenviarCredenciales(@PathVariable Integer id) {
    usuarioService.reenviarCredenciales(id);
    return ResponseEntity.ok("Se envió una nueva contraseña temporal al correo del usuario.");
  }

  @PostMapping("/push-token")
  // Cualquier usuario logueado (técnico, integrante, arquitecto) puede mandar su token
  public ResponseEntity<Void> guardarPushToken(@RequestBody Map<String, String> body, Principal principal) {

    String token = body.get("pushToken");

    // principal.getName() trae el email (username) del usuario que tiene la sesión iniciada
    String emailUsuarioLogueado = principal.getName();

    usuarioService.actualizarPushToken(emailUsuarioLogueado, token);

    return ResponseEntity.ok().build();
  }
}