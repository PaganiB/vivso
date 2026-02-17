package com.vivso.Vivso.Service;

import com.vivso.Vivso.Model.Organizacion;
import com.vivso.Vivso.Repository.IOrganizacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrganizacionService implements IOrganizacionService{

    @Autowired
    private IOrganizacionRepository organizacionRepository;

    @Override
    public List<Organizacion> listarTodas() {
        return organizacionRepository.findAll();
    }

    @Override
    public Optional<Organizacion> buscarPorCuit(String cuit) {
        if(!organizacionRepository.existsOrganizacionByCuit(cuit)){
            throw new RuntimeException("No existe la organización con CUIT " + cuit);
        }
        return organizacionRepository.findOrganizacionByCuit(cuit);
    }

    @Override
    public Organizacion guardar(Organizacion organizacion) {
        return organizacionRepository.save(organizacion);
    }

    @Override
    public void eliminar(String cuit) {
        if(!organizacionRepository.existsOrganizacionByCuit(cuit)){
            throw new RuntimeException("No existe la organización con CUIT " + cuit);
        }
        organizacionRepository.deleteByCuit(cuit);
    }

    @Override
    public List<Organizacion> buscarPorTipo(String tipo) {
        return List.of();
    }

    @Override
    public Optional<Organizacion> buscarPorNombre(String nombre) {
        if(!organizacionRepository.existsOrganizacionByCuit(nombre)){
            throw new RuntimeException("No existe la organización con nombre " + nombre);
        }
        return organizacionRepository.findOrganizacionByNombre(nombre);
    }
}
