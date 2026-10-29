package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.*;
import com.vivso.Vivso.Exception.RecursoNoEncontradoException;
import com.vivso.Vivso.Exception.ReglaDeNegocioException;
import com.vivso.Vivso.Model.Integrante;
import com.vivso.Vivso.Model.Organizacion;
import com.vivso.Vivso.Model.TipoCargo;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Repository.IIntegranteRepository;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import com.vivso.Vivso.Security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
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
    @Autowired
    private IIntegranteRepository integranteRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> iniciarSesion(UsuarioLoginDTO loginDTO) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new ReglaDeNegocioException("Correo o contraseña incorrectos.", HttpStatus.UNAUTHORIZED);
        } catch (DisabledException e) {
            throw new ReglaDeNegocioException("Tu cuenta está desactivada. Contactá al administrador.", HttpStatus.UNAUTHORIZED);
        }

        // Buscamos el usuario para obtener el rol
        Usuario usuario = usuarioRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        String tokenGenerado = jwtUtils.generarToken(usuario.getEmail());

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("token", tokenGenerado);
        respuesta.put("mensaje", "Login exitoso");
        respuesta.put("rol", usuario.getRol());
        respuesta.put("id", String.valueOf(usuario.getId()));
        respuesta.put("debeCambiarPassword", usuario.getDebeCambiarPassword());

        // Solo los integrantes pertenecen a una organización; el resto no recibe esta clave
        integranteRepository.findByUsuario_Email(usuario.getEmail())
                .ifPresent(i -> respuesta.put("cuitOrg", i.getOrganizacion().getCuit()));

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
    // nuevoi
    @Transactional(readOnly = true)
    public PerfilDTO obtenerPerfil(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        PerfilDTO.PerfilDTOBuilder perfil = PerfilDTO.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido());

        if ("INTEGRANTE".equalsIgnoreCase(usuario.getRol())) {
            integranteRepository.findByUsuario_Email(email).ifPresent(i -> {
                perfil.nombre(i.getNombre())                 // <-- vuelve: pisa lo de Usuario
                        .apellido(i.getApellido())             // <-- vuelve
                        .cargo(i.getCargo() != null ? i.getCargo().name() : null);

                Organizacion org = i.getOrganizacion();
                List<Integrante> integrantes = integranteRepository.findByOrganizacion_Cuit(org.getCuit());

                perfil.organizacion(OrganizacionResumenDTO.builder()
                        .cuit(org.getCuit())
                        .nombre(org.getNombre())
                        .presidente(nombreCompletoDe(integrantes, TipoCargo.PRESIDENTE))
                        .tesorero(nombreCompletoDe(integrantes, TipoCargo.TESORERO))
                        .build());
            });
        }

        return perfil.build();
    }

    private String nombreCompletoDe(List<Integrante> integrantes, TipoCargo cargo) {
        return integrantes.stream()
                .filter(i -> i.getCargo() == cargo && Boolean.TRUE.equals(i.getActivo()))
                .findFirst()
                .map(i -> i.getNombre() + " " + i.getApellido())
                .orElse(null);
    }
}