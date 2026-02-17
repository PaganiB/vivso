package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.Organizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IOrganizacionRepository extends JpaRepository<Organizacion, Integer> {
    Optional<Organizacion> findOrganizacionByCuit(String cuit);
    boolean existsOrganizacionByCuit(String cuit);
    void deleteByCuit(String cuit);
    Optional<Organizacion> findOrganizacionByNombre(String nombre);
}
