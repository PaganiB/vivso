package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.IntegranteOrganizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IOrganizacionIntegranteRepository extends JpaRepository<IntegranteOrganizacion, Integer> {
    List<IntegranteOrganizacion> findByOrganizacion_Cuit(String cuitOrg);
    List<IntegranteOrganizacion> findByActivoSi();
}
