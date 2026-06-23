package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.UsuarioLoginDTO;
import com.vivso.Vivso.DTO.UsuarioRegistroDTO;
import com.vivso.Vivso.DTO.UsuarioRespuestaDTO;
import com.vivso.Vivso.Service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody UsuarioLoginDTO loginDTO) {
        // El Controller solo delega la tarea y devuelve un HTTP 200 OK
        Map<String, String> respuesta = authService.iniciarSesion(loginDTO);
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioRespuestaDTO> registrarUsuario(@Valid @RequestBody UsuarioRegistroDTO registroDTO) {
        // Delega la tarea y devuelve un HTTP 201 CREATED
        UsuarioRespuestaDTO respuesta = authService.registrarUsuario(registroDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}