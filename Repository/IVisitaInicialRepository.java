package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.VisitaInicial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface IVisitaInicialRepository extends JpaRepository<VisitaInicial, Integer> {

  List<VisitaInicial> findByTecnico_Id(Integer idTecnico);

  List<VisitaInicial> findBySolicitud_IdSolicitudOrderByFechaVisitaDesc(Integer idSolicitud);

  boolean existsBySolicitud_IdSolicitud(Integer idSolicitud);

  @Query("SELECT v.solicitud.idSolicitud FROM VisitaInicial v WHERE v.solicitud.idSolicitud IN :ids")
  Set<Integer> findIdsConVisita(@Param("ids") List<Integer> ids);
}