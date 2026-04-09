package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.ViviendaDTO;
import com.vivso.Vivso.Model.EstadoVivienda;

import java.util.List;
import java.util.Optional;

public interface IViviendaService {
    List<ViviendaDTO> listarTodas();

    ViviendaDTO buscarPorExpediente(String numExp);

    ViviendaDTO guardar(ViviendaDTO viviendaDTO);

    void eliminar(String numExp);

    // Filtrar por estado (Iniciada, Avanzada, Finalizada)
    List<ViviendaDTO> buscarPorEstado(EstadoVivienda estado);

    // Buscar la vivienda adjudicada a una familia
    Optional<ViviendaDTO> buscarPorFamilia(Integer idFamilia);

    ViviendaDTO actualizar(String numExp, ViviendaDTO viviendaDTO);
}
