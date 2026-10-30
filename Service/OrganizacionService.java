package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.OrganizacionDTO;
import com.vivso.Vivso.Exception.RecursoNoEncontradoException;
import com.vivso.Vivso.Exception.ReglaDeNegocioException;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.Organizacion;
import com.vivso.Vivso.Repository.IOrganizacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizacionService implements IOrganizacionService {

    @Autowired private IOrganizacionRepository orgRepo;
    @Autowired private VivsoMapper mapper;

    @Override
    public List<OrganizacionDTO> getOrganizacion() {
        return orgRepo.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public OrganizacionDTO saveOrganizacion(OrganizacionDTO dto) {
        if (orgRepo.existsById(dto.getCuit()))
            throw new ReglaDeNegocioException("Ese CUIT ya existe: " + dto.getCuit(), HttpStatus.CONFLICT);
        return mapper.toDTO(orgRepo.save(mapper.toEntity(dto)));
    }

    @Override
    public OrganizacionDTO updateOrganizacion(String cuit, OrganizacionDTO dto) {
        Organizacion o = orgRepo.findById(cuit)
                .orElseThrow(() -> new RecursoNoEncontradoException("Organización no encontrada: " + cuit));

        // Aplica solo los campos no nulos — sin if por campo, el CUIT (PK) está ignorado en el mapper
        mapper.updateFromDto(dto, o);

        return mapper.toDTO(orgRepo.save(o));
    }

    @Override
    public void deleteOrganizacion(String cuit) {
        Organizacion o = orgRepo.findById(cuit)
                .orElseThrow(() -> new RecursoNoEncontradoException("Organización no encontrada: " + cuit));
        orgRepo.delete(o);
    }

    @Override
    public OrganizacionDTO buscarPorCuit(String cuit) {
        return orgRepo.findById(cuit)
                .map(mapper::toDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Organización no encontrada: " + cuit));
    }

    // buscarPorTipo
    @Override
    public List<OrganizacionDTO> buscarPorTipo(String tipo) {
        List<Organizacion> result = orgRepo.findOrganizacionByTipo(tipo);
        return result.stream().map(mapper::toDTO).toList();
    }

    // buscarPorNombre
    @Override
    public List<OrganizacionDTO> buscarPorNombre(String nombre) {
        List<Organizacion> result = orgRepo.findByNombreContainingIgnoreCase(nombre);
        return result.stream().map(mapper::toDTO).toList();
    }
}
