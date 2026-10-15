package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.FamiliarDTO;
import com.vivso.Vivso.Exception.RecursoNoEncontradoException;
import com.vivso.Vivso.Exception.ReglaDeNegocioException;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.Familia;
import com.vivso.Vivso.Model.Familiar;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Repository.IFamiliarRepository;
import com.vivso.Vivso.Repository.IFamiliaRepository;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class FamiliarService implements IFamiliarService {

    @Autowired private IFamiliarRepository familiarRepository;
    @Autowired private IFamiliaRepository familiaRepo;
    @Autowired private IUsuarioRepository usuarioRepo;
    @Autowired private VivsoMapper mapper;

    @Override
    public List<FamiliarDTO> listarTodos() {
        return familiarRepository.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public FamiliarDTO buscarPorId(Integer id) {
        return familiarRepository.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Familiar no encontrado: " + id));
    }

    @Override
    public FamiliarDTO guardar(FamiliarDTO dto) {
        Familia fam = familiaRepo.findById(dto.getFamilia())
                .orElseThrow(() -> new RecursoNoEncontradoException("Familia no encontrada: " + dto.getFamilia()));

        Familiar familiar = mapper.toEntity(dto);   // Mapea todos los campos simples + defaultValue condicion_especial
        familiar.setFamilia(fam);

        return mapper.toDTO(familiarRepository.save(familiar));
    }

    @Override
    public FamiliarDTO actualizar(Integer id, FamiliarDTO dto) {
        Familiar familiar = familiarRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Familiar no encontrado: " + id));

        // Aplica solo los campos no nulos del DTO — sin if por campo
        mapper.updateFromDto(dto, familiar);

        // Relaciones siempre se resuelven manualmente
        Familia fam = familiaRepo.findById(dto.getFamilia())
                .orElseThrow(() -> new RecursoNoEncontradoException("Familia no encontrada: " + dto.getFamilia()));
        familiar.setFamilia(fam);

        return mapper.toDTO(familiarRepository.save(familiar));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!familiarRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe integrante de la familia con el ID: " + id);
        }
        familiarRepository.deleteById(id);
    }

    @Override
    public FamiliarDTO buscarPorDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            throw new ReglaDeNegocioException("El DNI proporcionado no es válido.", HttpStatus.BAD_REQUEST);
        }
        return familiarRepository.findByDni(dni)
                .map(mapper::toDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró ningún familiar con el DNI: " + dni));
    }

    @Override
    public List<FamiliarDTO> buscarPorFamilia(Integer id_familia) {
        if (!familiaRepo.existsById(id_familia)) {
            throw new RecursoNoEncontradoException("Familia no encontrada: " + id_familia);
        }
        return familiarRepository.findByFamilia_IdFamilia(id_familia).stream()
                .map(mapper::toDTO)
                .toList();
    }
}
