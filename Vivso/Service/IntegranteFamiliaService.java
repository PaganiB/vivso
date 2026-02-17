package com.vivso.Vivso.Service;

import com.vivso.Vivso.Model.IntegranteFamilia;
import com.vivso.Vivso.Repository.IFamiliaIntegranteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class IntegranteFamiliaService implements IFamiliaIntegranteService{

    @Autowired
    private IFamiliaIntegranteRepository integranteRepository;

    @Override
    public List<IntegranteFamilia> listarTodos() {
        return integranteRepository.findAll();
    }

    @Override
    public IntegranteFamilia buscarPorId(Integer id) {
        return integranteRepository.findById(id).orElse(null);
    }

    @Override
    public IntegranteFamilia guardar(IntegranteFamilia integrante) {
        integranteRepository.save(integrante);

        return integrante;
    }

    @Override
    public void eliminar(Integer id) {
        if(!integranteRepository.existsById(id)){
            throw new RuntimeException("No existe integrante de la familia con el ID: " + id);
        }

        integranteRepository.deleteById(id);
    }

    @Override
    public Optional<IntegranteFamilia> buscarPorDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            return Optional.empty();
        }

        return integranteRepository.findByDni(dni);
    }

    @Override
    public List<IntegranteFamilia> buscarPorFamilia(Integer idFamilia) {
        // Validamos que el ID sea válido
        if (idFamilia == null || idFamilia <= 0) {
            return Collections.emptyList();
        }

        // Retornamos la lista de integrantes asociados a esa familia
        return integranteRepository.findByFamilia_IdFamilia(idFamilia);
    }
}
