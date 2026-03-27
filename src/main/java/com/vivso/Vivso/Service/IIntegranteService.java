package com.vivso.Vivso.Service;

import com.vivso.Vivso.Model.Integrante;

import java.util.List;

public interface IIntegranteService {

    List<Integrante> listarTodos();

    Integrante guardar(Integrante integrante);

    void eliminar(Integer id);

    // Filtrar por organización (CUIT)
    List<Integrante> listarPorOrganizacion(String cuitOrg);

    // Buscar integrantes activos o por función
    List<Integrante> buscarActivos();

}
