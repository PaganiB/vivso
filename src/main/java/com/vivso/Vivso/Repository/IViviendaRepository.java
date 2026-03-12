package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.EstadoVivienda;
import com.vivso.Vivso.Model.Vivienda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IViviendaRepository extends JpaRepository<Vivienda, Integer> {
    Optional<Vivienda> findViviendaByNumExp(String numExp);

    boolean existsByNumExp(String numExp);

    void deleteViviendaByNumExp(String numExp);

    List<Vivienda> findViviendaByEstado(EstadoVivienda estado);
}
