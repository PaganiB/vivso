package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.*;
import com.vivso.Vivso.Service.AuthService;
import com.vivso.Vivso.Service.IUsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private IUsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody UsuarioLoginDTO loginDTO) {
        Map<String, Object> respuesta = authService.iniciarSesion(loginDTO);
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioRespuestaDTO> registrarUsuario(@Valid @RequestBody UsuarioRegistroDTO registroDTO) {
        // Delega la tarea y devuelve un HTTP 201 CREATED
        UsuarioRespuestaDTO respuesta = authService.registrarUsuario(registroDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PatchMapping("/cambiar-password")
    public ResponseEntity<String> cambiarPassword(Authentication auth, @Valid @RequestBody PasswordUpdateDTO dto) {
        usuarioService.actualizarPassword(auth.getName(), dto);
        return ResponseEntity.ok("Contraseña actualizada con éxito.");
    }

    @GetMapping("/me")
    public ResponseEntity<PerfilDTO> miPerfil(Authentication auth) {
        return ResponseEntity.ok(authService.obtenerPerfil(auth.getName()));
    }
}