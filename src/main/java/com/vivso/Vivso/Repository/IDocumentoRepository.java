package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.Documento;
import com.vivso.Vivso.Model.Familia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface IDocumentoRepository extends JpaRepository<Documento, Integer> {
    // Busca por el ID de la entidad Familia que está dentro de Documento
    @Query("SELECT d FROM Documento d WHERE d.familia.id_familia = :idFamilia")
    List<Documento> findByFamilia_IdFamilia(Integer idFamilia);

    // Busca por el CUIT de la entidad Organizacion que está dentro de Documento
    List<Documento> findByOrganizacion_Cuit(String cuit);

    // Busca por el ID del Usuario revisor
    List<Documento> findByRevisor_Id(Integer idUsuario);
}
