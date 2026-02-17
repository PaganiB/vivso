package com.vivso.Vivso.Service;

import com.vivso.Vivso.Model.IntegranteFamilia;

import java.util.List;
import java.util.Optional;

public interface IFamiliaIntegranteService {
    List<IntegranteFamilia> listarTodos();
    IntegranteFamilia buscarPorId(Integer id);
    IntegranteFamilia guardar(IntegranteFamilia integrante);
    void eliminar(Integer id);

    // Búsqueda por DNI (según estructura SQL)
    Optional<IntegranteFamilia> buscarPorDni(String dni);

    // Recuperar integrantes de una familia específica
    List<IntegranteFamilia> buscarPorFamilia(Integer idFamilia);
}
