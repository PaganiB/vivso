package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.IntegranteFamiliaDTO;
import com.vivso.Vivso.Model.IntegranteFamilia;

import java.util.List;
import java.util.Optional;

public interface IFamiliaIntegranteService {
    List<IntegranteFamiliaDTO> listarTodos();

    IntegranteFamiliaDTO buscarPorId(Integer id);

    IntegranteFamiliaDTO guardar(IntegranteFamiliaDTO integrante);

    void eliminar(Integer id);

    // Búsqueda por DNI (según estructura SQL)
    Optional<IntegranteFamiliaDTO> buscarPorDni(String dni);

    // Recuperar integrantes de una familia específica
    List<IntegranteFamiliaDTO> buscarPorFamilia(Integer id_familia);
}
