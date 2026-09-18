package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.EstadoVivienda;
import com.vivso.Vivso.Model.Vivienda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IViviendaRepository extends JpaRepository<Vivienda, Integer> {

    List<Vivienda> findViviendaByEstado(EstadoVivienda estado);

    @Query("SELECT v FROM Vivienda v WHERE v.familia.idFamilia = :id")
    Optional<Vivienda> findByFamilia_IdFamilia(Integer idFamilia);

    List<Vivienda> findByLocalidadContainingIgnoreCase(String localidad);

    @Query("SELECT v FROM Vivienda v WHERE YEAR(v.fechaInic) = :anio")
    List<Vivienda> findByAnioInicio(@Param("anio") int anio);

    @Query("SELECT v FROM Vivienda v WHERE YEAR(v.fechaFin) = :anio")
    List<Vivienda> findByAnioFin(@Param("anio") int anio);

    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END " +
            "FROM Vivienda v JOIN v.familia f JOIN f.familiares fam " +
            "WHERE fam.dni = :dni AND v.estado = 'FINALIZADA' AND v.fechaFin >= :fechaLimite")
    boolean existeViviendaEntregadaEnUltimos5Anios(@Param("dni") String dni, @Param("fechaLimite") LocalDate fechaLimite);
}