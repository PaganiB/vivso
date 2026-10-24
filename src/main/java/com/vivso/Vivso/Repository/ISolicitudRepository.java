package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.EstadoSolicitud;
import com.vivso.Vivso.Model.Organizacion;
import com.vivso.Vivso.Model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISolicitudRepository extends JpaRepository<Solicitud, Integer> {
  List<Solicitud> findByEstado(EstadoSolicitud estado);

  List<Solicitud> findByCuitOrg_Cuit(String cuit);

  // 1. Para el Dashboard Web (Trae todas las asignadas al técnico)
  @Query("SELECT s FROM Solicitud s " +
    "LEFT JOIN FETCH s.familiaBeneficiaria " +
    "LEFT JOIN FETCH s.cuitOrg " +
    "WHERE s.tecnicoAsignado.id = :idTecnico")
  List<Solicitud> buscarDashboardWebPorTecnico(@Param("idTecnico") Integer idTecnico);

  // 2. Para la App Móvil (Ya filtrado por estado desde la Base de Datos)
  @Query("SELECT s FROM Solicitud s " +
    "LEFT JOIN FETCH s.familiaBeneficiaria " +
    "LEFT JOIN FETCH s.cuitOrg " +
    "WHERE s.tecnicoAsignado.id = :idTecnico " +
    "AND s.estado IN (:estados) " +
    "AND NOT EXISTS (SELECT v FROM VisitaInicial v WHERE v.solicitud.idSolicitud = s.idSolicitud)")
  List<Solicitud> buscarSolicitudesParaMovil(
    @Param("idTecnico") Integer idTecnico,
    @Param("estados") List<EstadoSolicitud> estados);

  // 3. Para la App Móvil del Integrante ONG (Filtra por ONG y estado Aprobada)
  @Query("SELECT s FROM Solicitud s WHERE s.cuitOrg.cuit = :cuit AND s.estado = 'Aprobada'")
  List<Solicitud> buscarObrasParaOng(@Param("cuit") String cuit);

  // 4. Para la App Móvil: Trae obras aprobadas + Filtra las que tienen etapas + Empaqueta Familia
  @Query("SELECT s FROM Solicitud s " +
    "LEFT JOIN FETCH s.familiaBeneficiaria " +
    "WHERE s.cuitOrg.cuit = :cuitOng " +
    "AND s.estado = 'Aprobada' " +
    "AND EXISTS (SELECT e FROM EtapaObra e WHERE e.solicitud.idSolicitud = s.idSolicitud)")
  List<Solicitud> buscarObrasConEtapasParaOng(@Param("cuitOng") String cuitOng);

}
