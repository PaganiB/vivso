package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.PasswordUpdateDTO;
import com.vivso.Vivso.DTO.TecnicoDTO;
import com.vivso.Vivso.DTO.UsuarioRegistroDTO;
import com.vivso.Vivso.DTO.UsuarioRespuestaDTO;
import com.vivso.Vivso.Model.Usuario;

import java.util.List;

public interface IUsuarioService {
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

  void actualizarPassword(String email, PasswordUpdateDTO dto);

  void activar(Integer id);

  // Contraseña temporal aleatoria (12 caracteres) para cuentas creadas o reactivadas por el sistema
  String generarPasswordTemporal();

  // Genera una contraseña temporal nueva, prende la bandera de cambio obligatorio y se la manda por mail
  void reenviarCredenciales(Integer id);

  List<TecnicoDTO> listarTecnicosDisponibles();
}