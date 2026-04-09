package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.PasswordUpdateDTO;
import com.vivso.Vivso.DTO.UsuarioRegistroDTO;
import com.vivso.Vivso.DTO.UsuarioRespuestaDTO;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService implements IUsuarioService {

    @Autowired
    private IUsuarioRepository usuarioRepo;

    @Override
    public List<UsuarioRespuestaDTO> listarTodos() {
        return usuarioRepo.findAll().stream()
                .map(u -> new UsuarioRespuestaDTO(
                        u.getId(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getRol()))
                .toList();
    }

    @Override
    public UsuarioRespuestaDTO buscarPorId(Integer id) {
        return usuarioRepo.findById(id)
                .map(u -> new UsuarioRespuestaDTO(
                        u.getId(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getRol()))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID " + id)
                );
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
    public List<UsuarioRespuestaDTO> buscarPorRol(String rol) {
        return usuarioRepo.findByRol(rol).stream()
                .map(u -> new UsuarioRespuestaDTO(
                        u.getId(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getRol()))
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

    @Override
    public void activar(Integer id) {
        Usuario usuario = usuarioRepo.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID " + id));
        usuario.setActivo(true);
        usuarioRepo.save(usuario);
    }

    @Override
    public UsuarioRespuestaDTO actualizar(Integer id, UsuarioRegistroDTO dto) {
        // 1. Buscamos el usuario existente
        Usuario usuario = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID " + id));

        // 2. Validamos si el nuevo email ya lo tiene OTRO usuario
        if (!usuario.getEmail().equals(dto.getEmail()) && usuarioRepo.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("El nuevo email ya está registrado por otro usuario.");
        }

        // 3. Actualizamos los campos
        usuario.setUsername(dto.getUsername());
        usuario.setEmail(dto.getEmail());
        usuario.setRol(dto.getRol());

        // 4. Guardamos y devolvemos el DTO
        Usuario actualizado = usuarioRepo.save(usuario);
        return new UsuarioRespuestaDTO(
                actualizado.getId(),
                actualizado.getUsername(),
                actualizado.getEmail(),
                actualizado.getRol());
    }

    @Override
    public void actualizarPassword(Integer id, PasswordUpdateDTO dto) {
        Usuario usuario = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID " + id));

        // VALIDACIÓN DE SEGURIDAD
        // Comparamos la contraseña enviada con la que está en la base de datos
        // Nota: Si usas BCrypt, aquí usarías passwordEncoder.matches()
        if (!usuario.getPassword_hash().equals(dto.getPasswordActual())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        // Actualizamos con la nueva
        usuario.setPassword_hash(dto.getPasswordNueva());
        usuarioRepo.save(usuario);
    }
}
