package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.FamiliarDTO;
import java.util.List;

public interface IFamiliarService {
    List<FamiliarDTO> listarTodos();

    FamiliarDTO buscarPorId(Integer id);

    FamiliarDTO guardar(FamiliarDTO integrante);

    FamiliarDTO actualizar(Integer id, FamiliarDTO dto);

    void eliminar(Integer id);

    // Búsqueda por DNI (según estructura SQL)
    FamiliarDTO buscarPorDni(String dni);

    // Recuperar integrantes de una familia específica
    List<FamiliarDTO> buscarPorFamilia(Integer id_familia);
}
