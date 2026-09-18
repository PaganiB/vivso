package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.PasswordUpdateDTO;
import com.vivso.Vivso.DTO.UsuarioRegistroDTO;
import com.vivso.Vivso.DTO.UsuarioRespuestaDTO;
import com.vivso.Vivso.Exception.RecursoNoEncontradoException;
import com.vivso.Vivso.Exception.ReglaDeNegocioException;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService implements IUsuarioService {

    @Autowired private IUsuarioRepository usuarioRepo;
    @Autowired private VivsoMapper mapper;
    @Autowired private PasswordEncoder passwordEncoder;

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
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
    }

    @Override
    public Usuario buscarPorUsername(String username) {
        if (username == null || username.trim().isEmpty())
            throw new ReglaDeNegocioException("El nombre de usuario no puede estar vacío", HttpStatus.BAD_REQUEST);
        return usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + username));
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
            throw new ReglaDeNegocioException("El email ya está registrado.", HttpStatus.CONFLICT);

        if (existePorUsername(dto.getUsername()))
            throw new ReglaDeNegocioException("El nombre de usuario ya existe.", HttpStatus.CONFLICT);

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setEmail(dto.getEmail());
        usuario.setRol(dto.getRol());
        usuario.setActivo(true);

        // Encriptar contraseña
        usuario.setPassword_hash(passwordEncoder.encode(dto.getPassword()));

        return usuarioRepo.save(usuario);
    }

    @Override
    public void desactivar(Integer id) {
        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
        u.setActivo(false);
        usuarioRepo.save(u);
    }

    @Override
    public void activar(Integer id) {
        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));        u.setActivo(true);
        usuarioRepo.save(u);
    }

    @Override
    public UsuarioRespuestaDTO actualizar(Integer id, UsuarioRegistroDTO dto) {
        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));

        if (!u.getEmail().equals(dto.getEmail()) && usuarioRepo.existsByEmail(dto.getEmail()))
            throw new ReglaDeNegocioException("El nuevo email ya está registrado por otro usuario.", HttpStatus.CONFLICT);

        u.setUsername(dto.getUsername());
        u.setEmail(dto.getEmail());
        u.setRol(dto.getRol());

        return mapper.toRespuestaDTO(usuarioRepo.save(u));
    }

    @Override
    public void actualizarPassword(Integer id, PasswordUpdateDTO dto) {
        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));

        if (!passwordEncoder.matches(dto.getPasswordActual(), u.getPassword_hash()))
            throw new ReglaDeNegocioException("La contraseña actual es incorrecta", HttpStatus.UNAUTHORIZED);

        u.setPassword_hash(passwordEncoder.encode(dto.getPasswordNueva()));
        usuarioRepo.save(u);
    }
}
