package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.Vivienda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IViviendaRepository extends JpaRepository<Vivienda, Integer> {
}
