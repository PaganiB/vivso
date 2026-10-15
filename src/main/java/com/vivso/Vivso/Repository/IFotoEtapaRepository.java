package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.EtapaObra;
import com.vivso.Vivso.Model.FotoEtapa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IFotoEtapaRepository extends JpaRepository<FotoEtapa, Long> {

    // 2. Devuelve List (muchas fotos) y busca por idEtapaObra
    List<FotoEtapa> findByEtapa_IdEtapaObra(Integer idEtapaObra);

    // 3. Este méttodo es útil para el Service (validar si ya subieron al menos una foto antes de enviar)
    boolean existsByEtapa_IdEtapaObra(Integer idEtapaObra);

}
