package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.Integrante;
import com.vivso.Vivso.Model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IIntegranteRepository extends JpaRepository<Integrante, Integer> {
    List<Integrante> findByOrganizacion_Cuit(String cuitOrg);
    void deleteByOrganizacion_Cuit(String cuit);
    List<Integrante> findByActivoTrue();
    Optional<Integrante> findByUsuario(Usuario usuario);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END " +
            "FROM Integrante i WHERE i.dni = :dni " +
            "AND i.activo = true " +
            "AND i.organizacion.cuit != :cuitOrg")
    boolean existsByDniAndActivoTrueAndOrganizacion_CuitNot(String dni, String cuitOrg);

    Optional<Integrante> findByUsuario_Email(String email);
}
