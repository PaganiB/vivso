package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.Familia;
import com.vivso.Vivso.Model.Familiar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IFamiliarRepository extends JpaRepository<Familiar, Integer> {
    Optional<Familiar> findByDni(String dni);
    @Query("SELECT i FROM Familiar i WHERE i.familia.idFamilia = :idFamilia")
    List<Familiar> findByFamilia_IdFamilia(@Param("idFamilia") Integer idFamilia);
}
