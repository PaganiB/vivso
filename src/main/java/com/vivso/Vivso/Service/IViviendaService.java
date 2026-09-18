package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.ViviendaDTO;
import com.vivso.Vivso.Model.EstadoVivienda;

import java.util.List;
import java.util.Optional;

public interface IViviendaService {

    List<ViviendaDTO> listarTodas();

    ViviendaDTO guardar(ViviendaDTO viviendaDTO);

    Optional<ViviendaDTO> buscarPorFamilia(Integer idFamilia);

    ViviendaDTO actualizar(Integer idVivienda, ViviendaDTO viviendaDTO);

    void eliminar(Integer idVivienda);

    ViviendaDTO buscarPorId(Integer idVivienda);

    List<ViviendaDTO> buscarPorEstado(EstadoVivienda estado);

    List<ViviendaDTO> filtrarPorLocalidad(String localidad);

    List<ViviendaDTO> filtrarPorAnioFin(int anio);

    List<ViviendaDTO> filtrarPorAnioInicio(int anio);
}
