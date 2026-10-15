package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.PasswordUpdateDTO;
import com.vivso.Vivso.DTO.UsuarioRegistroDTO;
import com.vivso.Vivso.DTO.UsuarioRespuestaDTO;
import com.vivso.Vivso.Model.Usuario;

import java.util.List;
import java.util.Optional;

public interface  IUsuarioService {
    List<UsuarioRespuestaDTO> listarTodos();

    UsuarioRespuestaDTO buscarPorId(Integer id);

    // Buscar por nombre de usuario para el Login
    Usuario buscarPorUsername(String username);

    // Filtrar por rol de sistema
    List<UsuarioRespuestaDTO> buscarPorRol(String rol);

    boolean existePorEmail(String email);

    boolean existePorUsername(String username);

    Usuario registrarNuevoUsuario(UsuarioRegistroDTO registroDto);

    void desactivar(Integer id);

    UsuarioRespuestaDTO actualizar(Integer id, UsuarioRegistroDTO dto);

    void actualizarPassword(Integer id, PasswordUpdateDTO dto);

    void activar(Integer id);
}
