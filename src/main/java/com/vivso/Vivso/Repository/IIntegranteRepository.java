package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.Integrante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IIntegranteRepository extends JpaRepository<Integrante, Integer> {
    List<Integrante> findByOrganizacion_Cuit(String cuitOrg);

    List<Integrante> findByActivoTrue();
}
