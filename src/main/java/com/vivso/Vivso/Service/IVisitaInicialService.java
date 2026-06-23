package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.VisitaInicialDTO;
import java.util.List;

public interface IVisitaInicialService {

    // CRUD
    List<VisitaInicialDTO> getVisitas();

    VisitaInicialDTO saveVisita(VisitaInicialDTO visita);

    VisitaInicialDTO updateVisita(Integer idVisita, VisitaInicialDTO visita);

    void deleteVisita(Integer idVisita);

    //busqueda
    VisitaInicialDTO FindVisita(Integer idVisita);

    List<VisitaInicialDTO> FindVisitaPorTecnico(Integer tecnico);

}