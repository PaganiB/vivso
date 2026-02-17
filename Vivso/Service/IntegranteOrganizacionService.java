package com.vivso.Vivso.Service;

import com.vivso.Vivso.Model.IntegranteOrganizacion;
import com.vivso.Vivso.Repository.IOrganizacionIntegranteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class IntegranteOrganizacionService implements IOrganizacionIntegranteService{

    @Autowired
    private IOrganizacionIntegranteRepository integranteRepository;

    @Override
    public List<IntegranteOrganizacion> listarTodos() {
        return integranteRepository.findAll();
    }

    @Override
    public IntegranteOrganizacion guardar(IntegranteOrganizacion integrante) {
        integranteRepository.save(integrante);

        return integrante;
    }

    @Override
    public void eliminar(Integer id) {
        if (!integranteRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar: No existe el integrante con ID " + id);
        }

        // 2. Procedemos a la eliminación
        integranteRepository.deleteById(id);
    }

    @Override
    public List<IntegranteOrganizacion> listarPorOrganizacion(String cuitOrg) {
        // Validamos que el CUIT tenga el formato esperado o al menos no sea nulo
        if (cuitOrg == null || cuitOrg.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Retornamos la lista filtrada
        return integranteRepository.findByOrganizacion_Cuit(cuitOrg);
    }

    @Override
    public List<IntegranteOrganizacion> buscarActivos() {
        return integranteRepository.findByActivoSi();
    }
}
