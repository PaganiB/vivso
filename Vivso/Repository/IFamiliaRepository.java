package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.Familia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IFamiliaRepository extends JpaRepository<Familia, Integer> {
}
