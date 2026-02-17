package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.IntegranteFamilia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IFamiliaIntegranteRepository extends JpaRepository<IntegranteFamilia, Integer> {
    Optional<IntegranteFamilia> findByDni(String dni);
    List<IntegranteFamilia> findByFamilia_IdFamilia(Integer idFamilia);
}
