package com.vivso.Vivso.Service;
import com.vivso.Vivso.Model.IntegranteOrganizacion;
import java.util.List;

public interface IOrganizacionIntegranteService {

        List<IntegranteOrganizacion> listarTodos();
        IntegranteOrganizacion guardar(IntegranteOrganizacion integrante);
        void eliminar(Integer id);

        // Filtrar por organización (CUIT)
        List<IntegranteOrganizacion> listarPorOrganizacion(String cuitOrg);

        // Buscar integrantes activos o por función
        List<IntegranteOrganizacion> buscarActivos();

}
