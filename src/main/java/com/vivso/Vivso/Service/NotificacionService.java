package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.NotificacionDTO;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.Integrante;
import com.vivso.Vivso.Model.Notificacion;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Repository.IIntegranteRepository;
import com.vivso.Vivso.Repository.INotificacionRepository;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class NotificacionService {

  @Autowired
  private IIntegranteRepository integranteRepo;
  @Autowired
  private INotificacionRepository notificacionRepo;
  @Autowired
  private VivsoMapper vivsoMapper;
  @Autowired
  private ExpoPushClient expoPushClient;
  @Autowired
  private IUsuarioRepository usuarioRepo;

  @Async
  public void enviarPushMasivo(List<String> tokens, String titulo, String cuerpo, Map<String, String> datosExtra) {
    enviarPush(tokens, titulo, cuerpo, datosExtra);
  }

  private void enviarPushMulticast(List<String> tokens, String titulo, String cuerpo, String tipo, Integer idReferencia) {
    Map<String, String> data = Map.of(
      "tipo", tipo != null ? tipo : "",
      "idReferencia", idReferencia != null ? idReferencia.toString() : "");
    enviarPush(tokens, titulo, cuerpo, data);
  }

  private void enviarPush(List<String> tokens, String titulo, String cuerpo, Map<String, String> data) {
    if (tokens == null || tokens.isEmpty()) return;
    try {
      List<String> invalidos = expoPushClient.enviar(tokens, titulo, cuerpo, data);
      invalidos.forEach(usuarioRepo::limpiarPushToken);
      log.info("Push Expo: {} tokens procesados, {} dados de baja", tokens.size(), invalidos.size());
    } catch (Exception e) {
      log.error("Error enviando push por Expo: ", e);
    }
  }

  // 1. NOTIFICAR A TODA LA ONG (PERSISTENCIA + PUSH ASÍNCRONO)
  @Async
  @Transactional
  public void notificarOng(String cuitOng, String titulo, String mensaje, String tipo, Integer idReferencia) {
    List<Integrante> integrantes = integranteRepo.findByOrganizacion_Cuit(cuitOng);

    List<Notificacion> notificacionesAGuardar = new ArrayList<>();
    List<String> tokensPush = new ArrayList<>();

    for (Integrante integrante : integrantes) {
      Usuario usuario = integrante.getUsuario();
      if (usuario != null && Boolean.TRUE.equals(integrante.getActivo())) {
        // 1. Guardar registro en MySQL para cada integrante activo de la ONG
        Notificacion n = Notificacion.builder()
          .usuario(usuario)
          .titulo(titulo)
          .mensaje(mensaje)
          .tipo(tipo)
          .idReferencia(idReferencia)
          .fecha(LocalDateTime.now())
          .leida(false)
          .build();

        notificacionesAGuardar.add(n);

        // 2. Colectar token para el envío masivo de Firebase
        if (usuario.getPushToken() != null && !usuario.getPushToken().isBlank()) {
          tokensPush.add(usuario.getPushToken());
        }
      }
    }

    // Persistir en MySQL
    if (!notificacionesAGuardar.isEmpty()) {
      notificacionRepo.saveAll(notificacionesAGuardar);
    }

    // Enviar Push mediante Firebase FCM
    enviarPushMulticast(tokensPush, titulo, mensaje, tipo, idReferencia);
  }

  // 2. NOTIFICAR A UN USUARIO INDIVIDUAL (Ej: Al Técnico Asignado)
  @Async
  @Transactional
  public void notificarUsuario(Usuario usuario, String titulo, String mensaje, String tipo, Integer idReferencia) {
    if (usuario == null) return;

    // Persistir registro en MySQL
    Notificacion n = Notificacion.builder()
      .usuario(usuario)
      .titulo(titulo)
      .mensaje(mensaje)
      .tipo(tipo)
      .idReferencia(idReferencia)
      .fecha(LocalDateTime.now())
      .leida(false)
      .build();

    notificacionRepo.save(n);

    // Enviar Push individual si tiene token registrado
    if (usuario.getPushToken() != null && !usuario.getPushToken().isBlank()) {
      enviarPushMulticast(List.of(usuario.getPushToken()), titulo, mensaje, tipo, idReferencia);
    }
  }


  // 3. MÉTODOS DE CONSULTA Y GESTIÓN PARA LA APP MÓVIL
  @Transactional(readOnly = true)
  public List<NotificacionDTO> obtenerNotificacionesUsuario(String email) {
    List<Notificacion> notificaciones = notificacionRepo.findByUsuario_EmailOrderByFechaDesc(email);
    return vivsoMapper.toDTOList(notificaciones);
  }

  @Transactional(readOnly = true)
  public long contarNoLeidas(String email) {
    return notificacionRepo.countByUsuario_EmailAndLeidaFalse(email);
  }

  @Transactional
  public void marcarComoLeida(Integer idNotificacion, String email) {
    Notificacion n = notificacionRepo.findById(idNotificacion)
      .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));

    if (n.getUsuario().getEmail().equals(email)) {
      n.setLeida(true);
      notificacionRepo.save(n);
    }
  }

  @Transactional
  public void marcarTodasComoLeidas(String email) {
    notificacionRepo.marcarTodasComoLeidasPorUsuario(email);
  }

}
