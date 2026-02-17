package com.vivso.Vivso.Service;

import com.vivso.Vivso.Model.Organizacion;

import java.util.List;
import java.util.Optional;

public interface IOrganizacionService {

    // Recupera todas las organizaciones registradas
    List<Organizacion> listarTodas();

    // Busca una organización por su CUIT (Llave primaria)
    Optional<Organizacion> buscarPorCuit(String cuit);

    // Guarda una nueva organización o actualiza una existente
    Organizacion guardar(Organizacion organizacion);

    // Elimina una organización por su CUIT
    void eliminar(String cuit);

    // --- Métodos de búsqueda personalizados ---

    // Permite filtrar organizaciones por tipo (ej. 'ONG', 'Municipal')
    List<Organizacion> buscarPorTipo(String tipo);

    // Busca organizaciones que coincidan con un nombre o parte de él
    Optional<Organizacion> buscarPorNombre(String nombre);
}