package com.vivso.Vivso.Service;

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
    public List<Usuario> listarTodos() {
        return usuarioRepo.findAll();
    }

    @Override
    public Usuario buscarPorId(Integer id) {
        return usuarioRepo.findById(id).orElse(null);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        usuarioRepo.save(usuario);

        return usuario;
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
}
