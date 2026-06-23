package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.VisitaInicial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IVisitaInicialRepository extends JpaRepository<VisitaInicial,Integer> {

    List<VisitaInicial> findByTecnico_Id(Integer idTecnico);

}