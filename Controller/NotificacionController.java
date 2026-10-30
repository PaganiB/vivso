package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.NotificacionDTO;
import com.vivso.Vivso.Service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

  @Autowired
  private NotificacionService notificacionService;

  // Obtener todo el historial de notificaciones del usuario logueado
  @GetMapping
  public ResponseEntity<List<NotificacionDTO>> obtenerMisNotificaciones(Principal principal) {
    return ResponseEntity.ok(notificacionService.obtenerNotificacionesUsuario(principal.getName()));
  }

  // Obtener cantidad de notificaciones no leídas (para el número sobre la campanita)
  @GetMapping("/no-leidas/count")
  public ResponseEntity<Map<String, Long>> contarNoLeidas(Principal principal) {
    long count = notificacionService.contarNoLeidas(principal.getName());
    return ResponseEntity.ok(Map.of("noLeidas", count));
  }

  // Marcar una notificación específica como leída
  @PutMapping("/{idNotificacion}/marcar-leida")
  public ResponseEntity<Void> marcarComoLeida(@PathVariable Integer idNotificacion, Principal principal) {
    notificacionService.marcarComoLeida(idNotificacion, principal.getName());
    return ResponseEntity.noContent().build();
  }

  // Marcar todas las notificaciones del usuario como leídas
  @PutMapping("/marcar-todas-leidas")
  public ResponseEntity<Void> marcarTodasComoLeidas(Principal principal) {
    notificacionService.marcarTodasComoLeidas(principal.getName());
    return ResponseEntity.noContent().build();
  }
}