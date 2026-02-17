package com.vivso.Vivso.Service;

import com.vivso.Vivso.Model.Usuario;

import java.util.List;
import java.util.Optional;

public interface IUsuarioService {
    List<Usuario> listarTodos();
    Usuario buscarPorId(Integer id);
    Usuario guardar(Usuario usuario);

    // Buscar por nombre de usuario para el Login
    Usuario buscarPorUsername(String username);
    // Filtrar por rol de sistema
    List<Usuario> buscarPorRol(String rol);
}
