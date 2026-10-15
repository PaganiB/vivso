package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.VisitaInicialDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IVisitaInicialService {

    // CRUD
    List<VisitaInicialDTO> getVisitas();

    //Guardar VISITA v2
    VisitaInicialDTO saveVisita(VisitaInicialDTO dto, List<MultipartFile> fotos);
    VisitaInicialDTO updateVisita(Integer idVisita, VisitaInicialDTO visita);

    void deleteVisita(Integer idVisita);

    //busqueda
    VisitaInicialDTO FindVisita(Integer idVisita);

    List<VisitaInicialDTO> FindVisitaPorTecnico(Integer tecnico);

}