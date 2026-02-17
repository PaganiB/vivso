package com.vivso.Vivso.Service;

import com.vivso.Vivso.Model.Vivienda;

import java.util.List;
import java.util.Optional;

public interface IViviendaService {
    List<Vivienda> listarTodas();
    Optional<Vivienda> buscarPorExpediente(String numExp);
    Vivienda guardar(Vivienda vivienda);
    void eliminar(String numExp);

    // Filtrar por estado (Iniciada, Avanzada, Finalizada)
    List<Vivienda> buscarPorEstado(String estado);

    // Buscar la vivienda adjudicada a una familia
    Optional<Vivienda> buscarPorFamilia(Integer idFamilia);
}
