package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.Integrante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IOrganizacionIntegranteRepository extends JpaRepository<Integrante, Integer> {
    List<Integrante> findByCuitOrg(String cuitOrg);

    List<Integrante> findByActivoTrue();
}
