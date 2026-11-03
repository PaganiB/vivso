package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.OrganizacionDTO;
import com.vivso.Vivso.DTO.OrganizacionDesarrolloSocialDTO;

import java.util.List;

public interface IOrganizacionService {

    // CRUD/ABML
    List<OrganizacionDTO> getOrganizacion();

    OrganizacionDTO updateOrganizacion(String cuit, OrganizacionDTO organizacionDto);

    void deleteOrganizacion(String cuit);

    // --- Métodos de búsqueda personalizados ---

    // Busca una organización por su CUIT (Llave primaria)
    OrganizacionDTO buscarPorCuit(String cuit);

    // Permite filtrar organizaciones por tipo (ej. 'ONG', 'Municipal')
    List<OrganizacionDTO> buscarPorTipo(String tipo);

    // Busca organizaciones que coincidan con un nombre o parte de él
    List<OrganizacionDTO> buscarPorNombre(String nombre);

    List<OrganizacionDesarrolloSocialDTO> obtenerVistaDesarrolloSocial();
}