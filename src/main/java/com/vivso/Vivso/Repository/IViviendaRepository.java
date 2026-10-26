package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.Vivienda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface IViviendaRepository extends JpaRepository<Vivienda, Integer> {

  @Query("SELECT v FROM Vivienda v WHERE v.familia.idFamilia = :id")
  Optional<Vivienda> findByFamilia_IdFamilia(Integer idFamilia);

  List<Vivienda> findByLocalidadContainingIgnoreCase(String localidad);

  @Query("SELECT v FROM Vivienda v WHERE YEAR(v.fechaInic) = :anio")
  List<Vivienda> findByAnioInicio(@Param("anio") int anio);

  @Query("SELECT v FROM Vivienda v WHERE YEAR(v.fechaFin) = :anio")
  List<Vivienda> findByAnioFin(@Param("anio") int anio);

  // Busca la vivienda que le pertenece a una solicitud específica
  Optional<Vivienda> findBySolicitud_IdSolicitud(Integer idSolicitud);

  @Query("SELECT v.solicitud.idSolicitud FROM Vivienda v WHERE v.solicitud.idSolicitud IN :ids")
  Set<Integer> findIdsConVivienda(@Param("ids") List<Integer> ids);

}