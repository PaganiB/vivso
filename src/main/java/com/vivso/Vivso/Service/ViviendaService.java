package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.ViviendaDTO;
import com.vivso.Vivso.Mapper.Mapper;
import com.vivso.Vivso.Model.EstadoVivienda;
import com.vivso.Vivso.Model.Familia;
import com.vivso.Vivso.Model.Vivienda;
import com.vivso.Vivso.Repository.IFamiliaRepository;
import com.vivso.Vivso.Repository.IViviendaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ViviendaService implements IViviendaService {

    @Autowired
    private IViviendaRepository viviendaRepo;

    @Autowired
    private IFamiliaRepository familiaRepo;

    @Override
    public List<ViviendaDTO> listarTodas() {
        return viviendaRepo.findAll().stream()
                .map(Mapper::toDTO)
                .toList();
    }

    @Override
    public ViviendaDTO buscarPorExpediente(String numExp) {
        return viviendaRepo.findViviendaByNumExp(numExp)
                .map(Mapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Vivienda no encontrada con numero de expediente: " + numExp ));
    }

    @Override
    @Transactional
    public void eliminar(String numExp) {
        // Verificamos si existe antes de borrar para que no tire un error feo
        if (!viviendaRepo.existsById(numExp)) {
            throw new RuntimeException("No se puede eliminar: la vivienda " + numExp + " no existe.");
        }
        viviendaRepo.deleteById(numExp);
    }

    @Override
    public List<ViviendaDTO> buscarPorEstado(EstadoVivienda estado) {
        return viviendaRepo.findViviendaByEstado(estado).stream()
                .map(Mapper::toDTO)
                .toList();
    }

    @Override
    public Optional<ViviendaDTO> buscarPorFamilia(Integer idFamilia) {
        return viviendaRepo.findByFamiliaId_familia(idFamilia)
                .map(Mapper::toDTO);
    }

    @Override
    @Transactional
    public ViviendaDTO guardar(ViviendaDTO dto) {
        // 1. Buscamos la familia para asegurar la relación (Obligatoria)
        Familia fam = familiaRepo.findById(dto.getId_familia())
                .orElseThrow(() -> new RuntimeException("Familia no encontrada con ID: " + dto.getId_familia()));

        // 2. Construimos la ENTIDAD (Mapeo manual de DTO a Entidad)
        Vivienda v = Vivienda.builder()
                .numExp(dto.getNumExp())
                .departamento(dto.getDepartamento())
                .localidad(dto.getLocalidad())
                .barrio(dto.getBarrio())
                .direccion(dto.getDireccion())
                .lat(dto.getLat())
                .lng(dto.getLng())
                .superficie(dto.getSuperficie())
                .fechaInic(dto.getFechaInic())
                .fechaFin(dto.getFechaFin())
                .estado(dto.getEstado())
                .avanceObra(dto.getAvanceObra() != null ? dto.getAvanceObra() : 0) // Nuevo
                .clasificacion(dto.getClasificacion()) // Nuevo
                .tipoVivienda(dto.getTipoVivienda()) // Nuevo
                .cantDormitorios(dto.getCantDormitorios()) // Nuevo
                .observacion(dto.getObservacion())
                .familia(fam)
                .build();

        // 3. Guardamos y devolvemos el DTO (usando saveAndFlush para asegurar el ID)
        return Mapper.toDTO(viviendaRepo.saveAndFlush(v));
    }

    @Override
    @Transactional
    public ViviendaDTO actualizar(String numExp, ViviendaDTO dto) {
        Vivienda v = viviendaRepo.findById(numExp).orElseThrow(
                () -> new RuntimeException("Vivienda no encontrada")
        );

        v.setDepartamento(dto.getDepartamento());
        v.setLocalidad(dto.getLocalidad());
        v.setBarrio(dto.getBarrio());
        v.setDireccion(dto.getDireccion());
        v.setLat(dto.getLat());
        v.setLng(dto.getLng());
        v.setEstado(dto.getEstado());
        v.setAvanceObra(dto.getAvanceObra()); // Nuevo
        v.setClasificacion(dto.getClasificacion()); // Nuevo
        v.setTipoVivienda(dto.getTipoVivienda()); // Nuevo
        v.setCantDormitorios(dto.getCantDormitorios()); // Nuevo
        v.setObservacion(dto.getObservacion());

        v.setFamilia(familiaRepo.findById(dto.getId_familia())
                .orElseThrow(() -> new RuntimeException("Familia no encontrada")));

        return Mapper.toDTO(viviendaRepo.saveAndFlush(v));
    }

    @Override
    public List<ViviendaDTO> filtrarPorLocalidad(String localidad) {
        return viviendaRepo.findByLocalidadContainingIgnoreCase(localidad)
                .stream().map(Mapper::toDTO).toList();
    }

    @Override
    public List<ViviendaDTO> filtrarPorAnioInicio(int anio) {
        return viviendaRepo.findByAnioInicio(anio)
                .stream().map(Mapper::toDTO).toList();
    }

    @Override
    public List<ViviendaDTO> filtrarPorAnioFin(int anio) {
        return viviendaRepo.findByAnioFin(anio)
                .stream().map(Mapper::toDTO).toList();
    }
}
