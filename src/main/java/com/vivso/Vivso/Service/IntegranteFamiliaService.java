package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.FamiliaDTO;
import com.vivso.Vivso.DTO.IntegranteFamiliaDTO;
import com.vivso.Vivso.Mapper.Mapper;
import com.vivso.Vivso.Model.Familia;
import com.vivso.Vivso.Model.IntegranteFamilia;
import com.vivso.Vivso.Model.Organizacion;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Repository.IFamiliaIntegranteRepository;
import com.vivso.Vivso.Repository.IFamiliaRepository;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class IntegranteFamiliaService implements IFamiliaIntegranteService {

    @Autowired
    private IFamiliaIntegranteRepository integranteRepository;
    @Autowired
    private IFamiliaRepository familiaRepo;
    @Autowired
    private IUsuarioRepository  usuarioRepo;

    @Override
    public List<IntegranteFamiliaDTO> listarTodos() {
        return integranteRepository.findAll().stream()
                .map(Mapper::toDTO)
                .toList();
    }

    @Override
    public IntegranteFamiliaDTO buscarPorId(Integer id) {
        return integranteRepository.findById(id)
                .map(Mapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Familiar no encontrado con el ID: " + id));
    }

    @Override
    public IntegranteFamiliaDTO guardar(IntegranteFamiliaDTO dto) {
        // 1. Buscamos los objetos reales en la base de datos usando los nombres/IDs del DTO
        Familia fam = familiaRepo.findByNombreRepresentante(dto.getFamilia())
                .orElseThrow(() -> new RuntimeException("Familia no encontrada"));

        Usuario user = usuarioRepo.findByUsername(dto.getUsuario())
                .orElse(null);

        // 2. Construimos la ENTIDAD (usando los objetos reales)
        var iF = IntegranteFamilia.builder()
                .dni(dto.getDni())
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .condicion_especial(dto.getCondicion_especial())
                .familia(fam) // Pasamos el objeto Familia, no el String
                .usuario(user) // Pasamos el objeto Usuario, no el String
                .build();

        return Mapper.toDTO(integranteRepository.save(iF));
    }

    @Override
    public void eliminar(Integer id) {
        if (!integranteRepository.existsById(id)) {
            throw new RuntimeException("No existe integrante de la familia con el ID: " + id);
        }

        integranteRepository.deleteById(id);
    }

    @Override
    public Optional<IntegranteFamiliaDTO> buscarPorDni(String dni) {
        // 1. Validación de entrada
        if (dni == null || dni.trim().isEmpty()) {
            return Optional.empty();
        }

        // 2. Buscamos en el repo, y si existe, mapeamos a DTO
        return integranteRepository.findByDni(dni)
                .map(Mapper::toDTO);
    }

    @Override
    public List<IntegranteFamiliaDTO> buscarPorFamilia(Integer id_familia) {
        // 1. Validación de seguridad básica
        if (id_familia == null || id_familia <= 0) {
            return Collections.emptyList();
        }

        // 2. Buscamos por el ID de la relación, convertimos a Stream, mapeamos a DTO y listamos
        return integranteRepository.findByFamilia_Id_familia(id_familia).stream()
                .map(Mapper::toDTO)
                .toList();
    }
}
