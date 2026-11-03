package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Integer> {
  Optional<Usuario> findByUsername(String username);

  Optional<Usuario> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);

  List<Usuario> findByRol(String rol);

  @Modifying
  @Transactional
  @Query("UPDATE Usuario u SET u.pushToken = null WHERE u.pushToken = :token")
  void limpiarPushToken(@Param("token") String token);
}
