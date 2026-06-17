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
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private IUsuarioRepository usuarioRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Map<String, String> iniciarSesion(UsuarioLoginDTO loginDTO) {
        // 1. Validamos credenciales
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );

        // 2. Fabricamos el token
        String tokenGenerado = jwtUtils.generarToken(loginDTO.getUsername());

        // 3. Armamos la respuesta para que Feli y Javi la reciban en React
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("token", tokenGenerado);
        respuesta.put("mensaje", "Login exitoso");

        return respuesta;
    }

    @Transactional
    public UsuarioRespuestaDTO registrarUsuario(UsuarioRegistroDTO registroDTO) {
        // 1. Verificamos que no exista
        if (usuarioRepo.findByUsername(registroDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Error: El nombre de usuario ya está en uso.");
        }

        // 2. Mapeamos
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(registroDTO.getUsername());
        nuevoUsuario.setEmail(registroDTO.getEmail());
        nuevoUsuario.setRol(registroDTO.getRol());
        nuevoUsuario.setActivo(true);

        // 3. Encriptamos
        nuevoUsuario.setPassword_hash(passwordEncoder.encode(registroDTO.getPassword()));

        // 4. Guardamos
        Usuario usuarioGuardado = usuarioRepo.save(nuevoUsuario);

        // 5. Devolvemos el DTO limpio
        return UsuarioRespuestaDTO.builder()
                .id(usuarioGuardado.getId())
                .username(usuarioGuardado.getUsername())
                .email(usuarioGuardado.getEmail())
                .rol(usuarioGuardado.getRol())
                .activo(usuarioGuardado.getActivo())
                .build();
    }
}