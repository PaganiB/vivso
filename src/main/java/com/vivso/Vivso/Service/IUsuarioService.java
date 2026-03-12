package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.UsuarioRegistroDTO;
import com.vivso.Vivso.DTO.UsuarioRespuestaDTO;
import com.vivso.Vivso.Model.Usuario;

import java.util.List;
import java.util.Optional;

public interface IUsuarioService {
    List<UsuarioRespuestaDTO> listarTodos();
    UsuarioRespuestaDTO buscarPorId(Integer id);
    Usuario guardar(Usuario usuario);
    // Buscar por nombre de usuario para el Login
    Usuario buscarPorUsername(String username);
    // Filtrar por rol de sistema
    List<Usuario> buscarPorRol(String rol);
    boolean existePorEmail(String email);
    boolean existePorUsername(String username);
    Usuario registrarNuevoUsuario(UsuarioRegistroDTO registroDto);
    void desactivar(Integer id);
}
