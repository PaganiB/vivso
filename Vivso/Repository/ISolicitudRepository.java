package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.EstadoSolicitud;
import com.vivso.Vivso.Model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISolicitudRepository extends JpaRepository<Solicitud, Integer> {
    List<Solicitud> findByEstado(EstadoSolicitud estado);
}
