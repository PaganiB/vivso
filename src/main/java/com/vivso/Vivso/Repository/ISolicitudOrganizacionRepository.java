package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.EstadoSolicitud;
import com.vivso.Vivso.Model.SolicitudOrganizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ISolicitudOrganizacionRepository extends JpaRepository<SolicitudOrganizacion, Integer> {
    Optional<SolicitudOrganizacion> findByCuit(String cuit);
    boolean existsByCuit(String cuit);
    boolean existsByCuitAndEstadoIn(String cuit, List<EstadoSolicitud> estados);
    Optional<SolicitudOrganizacion> findByTokenEdicion(String tokenEdicion);
}