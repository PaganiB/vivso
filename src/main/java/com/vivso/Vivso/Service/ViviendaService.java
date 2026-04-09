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
                .numExp(dto.getNumExp()) // Recordá que este ID lo mandamos nosotros
                .departamento(dto.getDepartamento())
                .localidad(dto.getLocalidad())
                .direccion(dto.getDireccion())
                .superficie(dto.getSuperficie())
                .fechaInic(dto.getFechaInic())
                .fechaFin(dto.getFechaFin())
                .estado(dto.getEstado())
                .observacion(dto.getObservacion())
                .familia(fam) // Seteamos el objeto completo
                .build();

        // 3. Guardamos y devolvemos el DTO (usando saveAndFlush para asegurar el ID)
        return Mapper.toDTO(viviendaRepo.saveAndFlush(v));
    }

    @Override
    @Transactional
    public ViviendaDTO actualizar(String numExp, ViviendaDTO viviendaDTO) {
        Vivienda vivienda = viviendaRepo.findById(numExp).orElseThrow(
                () -> new  RuntimeException("Vivienda no encontrada con ID: " + numExp)
        );

        vivienda.setDepartamento(viviendaDTO.getDepartamento());
        vivienda.setLocalidad(viviendaDTO.getLocalidad());
        vivienda.setDireccion(viviendaDTO.getDireccion());
        vivienda.setFechaInic(viviendaDTO.getFechaInic());
        vivienda.setFechaFin(viviendaDTO.getFechaFin());
        vivienda.setEstado(viviendaDTO.getEstado());
        vivienda.setObservacion(viviendaDTO.getObservacion());

        vivienda.setFamilia(familiaRepo.findById(viviendaDTO.getId_familia())
                .orElseThrow(() -> new RuntimeException("Familia no encontrada")));

        return Mapper.toDTO(viviendaRepo.saveAndFlush(vivienda));
    }
}
