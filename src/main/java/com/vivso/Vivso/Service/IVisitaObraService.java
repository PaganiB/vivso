package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.VisitaObraDTO;
import java.util.List;

public interface IVisitaObraService {

    VisitaObraDTO registrarVisita(VisitaObraDTO dto);

    List<VisitaObraDTO> obtenerHistorialPorVivienda(Integer idVivienda);

}