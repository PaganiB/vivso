package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.EstadoVivienda;
import com.vivso.Vivso.Model.Vivienda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IViviendaRepository extends JpaRepository<Vivienda, String> {
    Optional<Vivienda> findViviendaByNumExp(String numExp);

    List<Vivienda> findViviendaByEstado(EstadoVivienda estado);

    @Query("SELECT v FROM Vivienda v WHERE v.familia.id_familia = :id")
    Optional<Vivienda> findByFamiliaId_familia(Integer idFamilia);
}
