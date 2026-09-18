package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.IntegranteDTO;
import com.vivso.Vivso.Exception.RecursoNoEncontradoException;
import com.vivso.Vivso.Exception.ReglaDeNegocioException;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.Integrante;
import com.vivso.Vivso.Model.Organizacion;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Repository.IIntegranteRepository;
import com.vivso.Vivso.Repository.IOrganizacionRepository;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class IntegranteService implements IIntegranteService {

    @Autowired private IIntegranteRepository integranteRepo;
    @Autowired private IOrganizacionRepository orgRepo;
    @Autowired private IUsuarioRepository usuarioRepo;
    @Autowired private VivsoMapper mapper;

    @Override
    public List<IntegranteDTO> getIntegrantes() {
        return integranteRepo.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public IntegranteDTO saveIntegrantes(IntegranteDTO dto) {
        Organizacion org = orgRepo.findById(dto.getCuitOrg())
                .orElseThrow(() -> new RecursoNoEncontradoException("Organización no encontrada: " + dto.getCuitOrg()));

        validarVigenciaOng(org);
        validarExclusividadMiembro(dto.getDni(), dto.getCuitOrg());

        Usuario user = dto.getUsuario() != null
                ? usuarioRepo.findByUsername(dto.getUsuario()).orElse(null)
                : null;

        Integrante integrante = mapper.toEntity(dto);
        integrante.setOrganizacion(org);
        integrante.setUsuario(user);

        return mapper.toDTO(integranteRepo.save(integrante));
    }

    @Override
    public IntegranteDTO updateIntegrantes(Integer id, IntegranteDTO dto) {
        Integrante i = integranteRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Integrante no encontrado: " + id));

        // Aplica solo los campos no nulos — sin if por campo
        mapper.updateFromDto(dto, i);

        if (dto.getUsuario() != null) {
            Usuario user = usuarioRepo.findByUsername(dto.getUsuario())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + dto.getUsuario()));
            i.setUsuario(user);
        }

        if (dto.getCuitOrg() != null) {
            Organizacion org = orgRepo.findById(dto.getCuitOrg())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Organización no encontrada: " + dto.getCuitOrg()));
            validarVigenciaOng(org);
            if (Boolean.TRUE.equals(i.getActivo())) {
                validarExclusividadMiembro(i.getDni(), dto.getCuitOrg());
            }
            i.setOrganizacion(org);
        }

        return mapper.toDTO(integranteRepo.save(i));
    }

    @Override
    public void deleteIntegrantes(Integer id) {
        Integrante i = integranteRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Integrante no encontrado: " + id));
        integranteRepo.delete(i);
    }

    @Override
    public List<IntegranteDTO> listarPorOrganizacion(String cuitOrg) {
        return integranteRepo.findByOrganizacion_Cuit(cuitOrg).stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<IntegranteDTO> buscarActivos() {
        return integranteRepo.findByActivoTrue().stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public void validarVigenciaOng(Organizacion ong) {
        LocalDate hoy = LocalDate.now();
        LocalDate limiteCancelacion = ong.getFechaVencimientoVigencia().minusMonths(3);
        if (hoy.isAfter(limiteCancelacion)) {
            throw new ReglaDeNegocioException("La ONG " + ong.getNombre() +
                    " no está habilitada. Su constancia vence en menos de 3 meses o ya está vencida.", HttpStatus.CONFLICT);
        }
    }

    private void validarExclusividadMiembro(String dni, String cuitOrg) {
        if (integranteRepo.existsByDniAndActivoTrueAndOrganizacion_CuitNot(dni, cuitOrg)) {
            throw new ReglaDeNegocioException("El ciudadano con DNI " + dni +
                    " ya es integrante activo de otra organización registrada.", HttpStatus.CONFLICT);
        }
    }
}
