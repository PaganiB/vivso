package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.UsuarioLoginDTO;
import com.vivso.Vivso.DTO.UsuarioRegistroDTO;
import com.vivso.Vivso.DTO.UsuarioRespuestaDTO;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import com.vivso.Vivso.Security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private IUsuarioRepository usuarioRepository;
    @Autowired
    private IUsuarioService usuarioService;

    public Map<String, String> iniciarSesion(UsuarioLoginDTO loginDTO) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
        );

        // Buscamos el usuario para obtener el rol
        Usuario usuario = usuarioRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String tokenGenerado = jwtUtils.generarToken(loginDTO.getEmail());

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("token", tokenGenerado);
        respuesta.put("mensaje", "Login exitoso");
        respuesta.put("rol", usuario.getRol());

        return respuesta;
    }

    @Transactional
    public UsuarioRespuestaDTO registrarUsuario(UsuarioRegistroDTO registroDTO) {

        Usuario usuarioGuardado = usuarioService.registrarNuevoUsuario(registroDTO);

        return UsuarioRespuestaDTO.builder()
                .id(usuarioGuardado.getId())
                .username(usuarioGuardado.getUsername())
                .email(usuarioGuardado.getEmail())
                .rol(usuarioGuardado.getRol())
                .activo(usuarioGuardado.getActivo())
                .build();
    }
}