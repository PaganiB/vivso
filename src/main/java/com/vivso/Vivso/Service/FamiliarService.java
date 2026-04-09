package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.FamiliarDTO;
import com.vivso.Vivso.Mapper.Mapper;
import com.vivso.Vivso.Model.Familia;
import com.vivso.Vivso.Model.Familiar;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Repository.IFamiliarRepository;
import com.vivso.Vivso.Repository.IFamiliaRepository;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class FamiliarService implements IFamiliarService {

    @Autowired
    private IFamiliarRepository familiarRepository;
    @Autowired
    private IFamiliaRepository familiaRepo;
    @Autowired
    private IUsuarioRepository  usuarioRepo;

    @Override
    public List<FamiliarDTO> listarTodos() {
        return familiarRepository.findAll().stream()
                .map(Mapper::toDTO)
                .toList();
    }

    @Override
    public FamiliarDTO buscarPorId(Integer id) {
        return familiarRepository.findById(id)
                .map(Mapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Familiar no encontrado con el ID: " + id));
    }

    @Override
    public FamiliarDTO guardar(FamiliarDTO dto) {
        // 1. Buscamos por ID (findById), que es mucho más seguro que el nombre
        Familia fam = familiaRepo.findById(dto.getFamilia())
                .orElseThrow(() -> new RuntimeException("Familia no encontrada con el ID: " + dto.getFamilia()));

        // 2. El usuario lo dejamos por username (si es lo que tenés en el DTO)
        Usuario user = null;
        if (dto.getUsuario() != null) {
            user = usuarioRepo.findByUsername(dto.getUsuario()).orElse(null);
        }

        // 3. Construimos la ENTIDAD
        var iF = Familiar.builder()
                .dni(dto.getDni())
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .condicion_especial(dto.getCondicion_especial() != null ?
                        dto.getCondicion_especial() : "No posee discapacidad")
                .familia(fam)
                .usuario(user)
                .build();


        return Mapper.toDTO(familiarRepository.save(iF));
    }

    @Override
    public FamiliarDTO actualizar(Integer id, FamiliarDTO dto) {
        // Buscamos el integrante existente o lanzamos error si no está
        Familiar familiar = familiarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se puede editar: familiar no encontrado con ID " + id));

        //  Actualizamos los campos
        familiar.setDni(dto.getDni());
        familiar.setNombre(dto.getNombre());
        familiar.setApellido(dto.getApellido());
        //Campo de condicion
        String condicion = (dto.getCondicion_especial() != null)
                ? dto.getCondicion_especial()
                : "No posee discapacidad";
        familiar.setCondicion_especial(condicion);

        // Relaciones (Opcionales/Obligatorias)
        // Buscamos la familia por el nombre que viene en el DTO
        Familia fam = familiaRepo.findById(dto.getFamilia())
                .orElseThrow(() -> new RuntimeException("Familia '" + dto.getFamilia() + "' no encontrada"));
        familiar.setFamilia(fam);

        // El usuario es opcional, si no existe queda en null
        Usuario user = usuarioRepo.findByUsername(dto.getUsuario()).orElse(null);
        familiar.setUsuario(user);

        // Guardamos y devolvemos el DTO actualizado
        return Mapper.toDTO(familiarRepository.save(familiar));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!familiarRepository.existsById(id)) {
            throw new RuntimeException("No existe integrante de la familia con el ID: " + id);
        }

        familiarRepository.deleteById(id);
    }

    @Override
    public FamiliarDTO buscarPorDni(String dni) {
        // 1. Validación de entrada: Si el DNI es nulo o vacío, lanzamos error de negocio
        if (dni == null || dni.trim().isEmpty()) {
            throw new RuntimeException("El DNI proporcionado no es válido.");
        }

        // 2. Buscamos en el repo.
        // Si lo encuentra, lo mapea.
        // Si NO lo encuentra, lanza la excepción que atrapará el GEH.
        return familiarRepository.findByDni(dni)
                .map(Mapper::toDTO)
                .orElseThrow(() -> new RuntimeException("No se encontró ningún familiar con el DNI: " + dni));
    }

    @Override
    public List<FamiliarDTO> buscarPorFamilia(Integer id_familia) {
        // 1. Validación de seguridad básica
        if (id_familia == null || id_familia <= 0) {
            return Collections.emptyList();
        }

        // 2. Buscamos por el ID de la relación, convertimos a Stream, mapeamos a DTO y listamos
        return familiarRepository.findByFamiliaId_familia(id_familia).stream()
                .map(Mapper::toDTO)
                .toList();
    }
}
