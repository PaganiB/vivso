package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.PasswordUpdateDTO;
import com.vivso.Vivso.DTO.UsuarioRegistroDTO;
import com.vivso.Vivso.DTO.UsuarioRespuestaDTO;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService implements IUsuarioService {

    @Autowired private IUsuarioRepository usuarioRepo;
    @Autowired private VivsoMapper mapper;

    @Override
    public List<UsuarioRespuestaDTO> listarTodos() {
        return usuarioRepo.findAll().stream()
                .map(mapper::toRespuestaDTO)
                .toList();
    }

    @Override
    public UsuarioRespuestaDTO buscarPorId(Integer id) {
        return usuarioRepo.findById(id)
                .map(mapper::toRespuestaDTO)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
    }

    @Override
    public Usuario buscarPorUsername(String username) {
        if (username == null || username.trim().isEmpty())
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío");
        return usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }

    @Override
    public List<UsuarioRespuestaDTO> buscarPorRol(String rol) {
        return usuarioRepo.findByRol(rol).stream()
                .map(mapper::toRespuestaDTO)
                .toList();
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
    public Usuario registrarNuevoUsuario(UsuarioRegistroDTO dto) {
        if (existePorEmail(dto.getEmail()))
            throw new RuntimeException("El email ya está registrado.");
        if (existePorUsername(dto.getUsername()))
            throw new RuntimeException("El nombre de usuario ya existe.");

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setEmail(dto.getEmail());
        usuario.setRol(dto.getRol());
        usuario.setActivo(true);
        usuario.setPassword_hash(dto.getPassword()); // TODO: reemplazar por BCrypt al implementar JWT

        return usuarioRepo.save(usuario);
    }

    @Override
    public void desactivar(Integer id) {
        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
        u.setActivo(false);
        usuarioRepo.save(u);
    }

    @Override
    public void activar(Integer id) {
        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
        u.setActivo(true);
        usuarioRepo.save(u);
    }

    @Override
    public UsuarioRespuestaDTO actualizar(Integer id, UsuarioRegistroDTO dto) {
        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));

        if (!u.getEmail().equals(dto.getEmail()) && usuarioRepo.existsByEmail(dto.getEmail()))
            throw new RuntimeException("El nuevo email ya está registrado por otro usuario.");

        u.setUsername(dto.getUsername());
        u.setEmail(dto.getEmail());
        u.setRol(dto.getRol());

        return mapper.toRespuestaDTO(usuarioRepo.save(u));
    }

    @Override
    public void actualizarPassword(Integer id, PasswordUpdateDTO dto) {
        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));

        // TODO: reemplazar por BCrypt cuando se implemente JWT
        if (!u.getPassword_hash().equals(dto.getPasswordActual()))
            throw new RuntimeException("La contraseña actual es incorrecta");

        u.setPassword_hash(dto.getPasswordNueva());
        usuarioRepo.save(u);
    }
}
