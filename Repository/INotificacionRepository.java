package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface INotificacionRepository extends JpaRepository<Notificacion, Integer> {

  // Obtiene las notificaciones del usuario ordenadas de la más reciente a la más antigua
  List<Notificacion> findByUsuario_EmailOrderByFechaDesc(String email);

  // Cuenta cuántas notificaciones no leídas tiene el usuario (para el badge de la campanita)
  long countByUsuario_EmailAndLeidaFalse(String email);

  // Marca todas las notificaciones de un usuario como leídas en una sola consulta SQL
  @Modifying
  @Query("UPDATE Notificacion n SET n.leida = true WHERE n.usuario.email = :email AND n.leida = false")
  void marcarTodasComoLeidasPorUsuario(@Param("email") String email);
}