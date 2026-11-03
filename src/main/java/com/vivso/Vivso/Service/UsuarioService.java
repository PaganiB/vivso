package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.PasswordUpdateDTO;
import com.vivso.Vivso.DTO.TecnicoDTO;
import com.vivso.Vivso.DTO.UsuarioRegistroDTO;
import com.vivso.Vivso.DTO.UsuarioRespuestaDTO;
import com.vivso.Vivso.Exception.RecursoNoEncontradoException;
import com.vivso.Vivso.Exception.ReglaDeNegocioException;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
public class UsuarioService implements IUsuarioService {

    @Autowired private IUsuarioRepository usuarioRepo;
    @Autowired private VivsoMapper mapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private IEmailService emailService;

    private static final String CARACTERES_PASSWORD =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
    private final SecureRandom random = new SecureRandom();

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
        usuario.setDebeCambiarPassword(true);
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());

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
        if (dto.getNombre() != null) u.setNombre(dto.getNombre()); //nuevo
        if (dto.getApellido() != null) u.setApellido(dto.getApellido()); //nuevo

        return mapper.toRespuestaDTO(usuarioRepo.save(u));
    }

    @Override
    public void actualizarPassword(String email, PasswordUpdateDTO dto) {
        Usuario u = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.getPasswordActual(), u.getPassword_hash()))
            throw new ReglaDeNegocioException("La contraseña actual es incorrecta", HttpStatus.BAD_REQUEST);

        if (passwordEncoder.matches(dto.getPasswordNueva(), u.getPassword_hash()))
            throw new ReglaDeNegocioException("La contraseña nueva no puede ser igual a la actual", HttpStatus.BAD_REQUEST);

        u.setPassword_hash(passwordEncoder.encode(dto.getPasswordNueva()));
        u.setDebeCambiarPassword(false);
        usuarioRepo.save(u);
    }

    @Override
    public String generarPasswordTemporal() {
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            password.append(CARACTERES_PASSWORD.charAt(random.nextInt(CARACTERES_PASSWORD.length())));
        }
        return password.toString();
    }

    @Override
    @Transactional
    public void reenviarCredenciales(Integer id) {
        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));

        if (!Boolean.TRUE.equals(u.getActivo()))
            throw new ReglaDeNegocioException("El usuario está desactivado. Activalo antes de reenviarle las credenciales.", HttpStatus.CONFLICT);

        if (u.getEmail() == null || u.getEmail().isBlank())
            throw new ReglaDeNegocioException("El usuario no tiene un correo registrado.", HttpStatus.BAD_REQUEST);

        String passwordTemporal = generarPasswordTemporal();
        u.setPassword_hash(passwordEncoder.encode(passwordTemporal));
        u.setDebeCambiarPassword(true);
        usuarioRepo.save(u);

        emailService.enviarReenvioCredenciales(u.getEmail(), passwordTemporal);
    }

    @Transactional
    public List<TecnicoDTO> listarTecnicosDisponibles() {
        // Usamos el méodo que ya tenías creado en UsuarioRepository
        List<Usuario> tecnicos = usuarioRepo.findByRol("TECNICO");

        return tecnicos.stream()
                .map(t -> TecnicoDTO.builder()
                        .id(t.getId())
                        .username(t.getNombreUsuarioReal())
                        .email(t.getEmail())
                        .build())
                .toList();
    }

  @Transactional
  public void actualizarPushToken(String email, String pushToken) {
    String token = (pushToken == null || pushToken.isBlank()) ? null : pushToken;
    if (token != null && !ExpoPushClient.esTokenValido(token)) {
      throw new ReglaDeNegocioException("Formato de push token inválido.", HttpStatus.BAD_REQUEST);
    }
    // 1) PRIMERO limpiar el token en cualquier usuario que lo tenga (dispositivo compartido)
    if (token != null) usuarioRepo.limpiarPushToken(token);

    // 2) RECIÉN AHORA cargar al usuario y asignarle el token
    Usuario usuario = usuarioRepo.findByEmail(email)
      .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con email: " + email));
    usuario.setPushToken(token);
    usuarioRepo.save(usuario);
  }
}
