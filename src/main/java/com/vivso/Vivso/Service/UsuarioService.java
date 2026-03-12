package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.UsuarioRegistroDTO;
import com.vivso.Vivso.DTO.UsuarioRespuestaDTO;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService implements IUsuarioService{

    @Autowired
    private IUsuarioRepository usuarioRepo;

    @Override
    public List<UsuarioRespuestaDTO> listarTodos() {
        return usuarioRepo.findAll().stream()
                .map(u -> new UsuarioRespuestaDTO(
                        u.getId_usuario(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getRol()))
                .toList();
    }

    @Override
    public UsuarioRespuestaDTO buscarPorId(Integer id) {
        return usuarioRepo.findById(id)
                .map(u -> new UsuarioRespuestaDTO(
                        u.getId_usuario(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getRol()))
                .orElseThrow(()-> new RuntimeException("Usuario no encontrado con ID " + id)
                );
    }

    @Override
    public Usuario guardar(Usuario usuario) {
       return usuarioRepo.save(usuario);
    }

    @Override
    public Usuario buscarPorUsername(String username) {
        // 1. Validación de entrada para evitar consultas innecesarias
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío");
        }

        // 2. Buscamos y manejamos la ausencia de datos en una sola línea
        return usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el nombre: " + username));
    }

    @Override
    public List<Usuario> buscarPorRol(String rol) {
        return List.of();
    }

    @Override
    public boolean existePorEmail(String email) {
        return usuarioRepo.existsByEmail(email);
    }

    @Override
    public boolean existePorUsername(String username) {
        return usuarioRepo.existsByUsername(username);
    }

    @Override
    public Usuario registrarNuevoUsuario(UsuarioRegistroDTO registroDto) {
        if (this.existePorEmail(registroDto.getEmail())) {
            throw new RuntimeException("El email ya está registrado.");
        }
        if (this.existePorUsername(registroDto.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya existe.");
        }
        Usuario usuario = new Usuario();

        usuario.setUsername(registroDto.getUsername());
        usuario.setEmail(registroDto.getEmail());
        usuario.setRol(registroDto.getRol());
        usuario.setActivo(true);

        usuario.setPassword_hash(registroDto.getPassword());

        return usuarioRepo.save(usuario);
    }

    @Override
    public void desactivar(Integer id) {
       Usuario usuario = usuarioRepo.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID " + id));
       usuario.setActivo(false);
       usuarioRepo.save(usuario);
    }
}
