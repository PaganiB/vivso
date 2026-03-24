package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.IntegranteFamilia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IFamiliaIntegranteRepository extends JpaRepository<IntegranteFamilia, Integer> {
    Optional<IntegranteFamilia> findByDni(String dni);
    //List<IntegranteFamilia> findByFamiliaId_familia(Integer id_familia);
    @Query("SELECT i FROM IntegranteFamilia i WHERE i.familia.id_familia = :id_familia")
    List<IntegranteFamilia> findByFamiliaId_familia(@Param("id_familia") Integer id_familia);
}
