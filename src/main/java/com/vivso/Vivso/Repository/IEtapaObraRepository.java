package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.EtapaObra;
import com.vivso.Vivso.Model.EtapaConstruccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IEtapaObraRepository extends JpaRepository<EtapaObra, Integer> {

    // Sin orden: el service ordena por número de etapa (ordenar el enum en SQL sería alfabético)
    List<EtapaObra> findBySolicitud_IdSolicitud(Integer idSolicitud);

    Optional<EtapaObra> findBySolicitud_IdSolicitudAndEtapa(Integer idSolicitud, EtapaConstruccion etapa);

    boolean existsBySolicitud_IdSolicitud(Integer idSolicitud);
}