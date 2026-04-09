package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.IntegranteDTO;

import java.util.List;

public interface IIntegranteService {

    //CRUD/ABML
    List<IntegranteDTO> getIntegrantes();

    IntegranteDTO saveIntegrantes(IntegranteDTO integranteDto);

    IntegranteDTO updateIntegrantes(Integer idIntegrante, IntegranteDTO integranteDto);

    void deleteIntegrantes(Integer idIntegrante);

    // Filtrar por organización (CUIT)
    List<IntegranteDTO> listarPorOrganizacion(String cuitOrg);

    // Buscar integrantes activos o por función
    List<IntegranteDTO> buscarActivos();

}
