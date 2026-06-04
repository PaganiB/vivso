package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.OrganizacionDTO;
import com.vivso.Vivso.Mapper.VivsoMapper;
import com.vivso.Vivso.Model.Organizacion;
import com.vivso.Vivso.Repository.IOrganizacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
            throw new RuntimeException("Ese CUIT ya existe: " + dto.getCuit());

        return mapper.toDTO(orgRepo.save(mapper.toEntity(dto)));
    }

    @Override
    public OrganizacionDTO updateOrganizacion(String cuit, OrganizacionDTO dto) {
        Organizacion o = orgRepo.findById(cuit)
                .orElseThrow(() -> new RuntimeException("Organización no encontrada: " + cuit));

        // Aplica solo los campos no nulos — sin if por campo, el CUIT (PK) está ignorado en el mapper
        mapper.updateFromDto(dto, o);

        return mapper.toDTO(orgRepo.save(o));
    }

    @Override
    public void deleteOrganizacion(String cuit) {
        Organizacion o = orgRepo.findById(cuit)
                .orElseThrow(() -> new RuntimeException("Organización no encontrada: " + cuit));
        orgRepo.delete(o);
    }

    @Override
    public OrganizacionDTO buscarPorCuit(String cuit) {
        return orgRepo.findById(cuit)
                .map(mapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Organización no encontrada: " + cuit));
    }

    @Override
    public List<OrganizacionDTO> buscarPorTipo(String tipo) {
        List<Organizacion> result = orgRepo.findOrganizacionByTipo(tipo);
        if (result.isEmpty()) throw new RuntimeException("Ninguna organización pertenece a este tipo");
        return result.stream().map(mapper::toDTO).toList();
    }

    @Override
    public List<OrganizacionDTO> buscarPorNombre(String nombre) {
        List<Organizacion> result = orgRepo.findByNombreContainingIgnoreCase(nombre);
        if (result.isEmpty()) throw new RuntimeException("Ninguna organización tiene o contiene ese nombre");
        return result.stream().map(mapper::toDTO).toList();
    }
}
