package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.VisitaObra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IVisitaObraRepository extends JpaRepository<VisitaObra, Integer> {

    // Trae todas las visitas de una vivienda, ordenadas de la más nueva a la más vieja
    List<VisitaObra> findByVivienda_IdViviendaOrderByFechaDesc(Integer idVivienda);

    // Trae todas las inspecciones que hizo un técnico específico
    List<VisitaObra> findByTecnico_Id(Integer idTecnico);
}