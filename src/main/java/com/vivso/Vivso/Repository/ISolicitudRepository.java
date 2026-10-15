package com.vivso.Vivso.Repository;

import com.vivso.Vivso.Model.EstadoSolicitud;
import com.vivso.Vivso.Model.Organizacion;
import com.vivso.Vivso.Model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISolicitudRepository extends JpaRepository<Solicitud, Integer> {
    List<Solicitud> findByEstado(EstadoSolicitud estado);
    List<Solicitud> findByCuitOrg_Cuit(String cuit);

    // 1. Para el Dashboard Web (Trae todas las asignadas al técnico)
    @Query("SELECT s FROM Solicitud s WHERE s.tecnicoAsignado.id = :idTecnico")
    List<Solicitud> buscarPorTecnicoAsignado(@Param("idTecnico") Integer idTecnico);

    // 2. Para la App Móvil (Ya filtrado por estado desde la Base de Datos)
    @Query("SELECT s FROM Solicitud s WHERE s.tecnicoAsignado.id = :idTecnico AND s.estado IN ('Pendiente', 'Aprobada')")
    List<Solicitud> buscarParaAppMovil(@Param("idTecnico") Integer idTecnico);

    // 3. Para la App Móvil del Integrante ONG (Filtra por ONG y estado Aprobada)
    @Query("SELECT s FROM Solicitud s WHERE s.cuitOrg.cuit = :cuit AND s.estado = 'Aprobada'")
    List<Solicitud> buscarObrasParaOng(@Param("cuit") String cuit);


}
