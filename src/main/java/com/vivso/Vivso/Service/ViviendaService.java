package com.vivso.Vivso.Service;

import com.vivso.Vivso.Model.EstadoVivienda;
import com.vivso.Vivso.Model.Vivienda;
import com.vivso.Vivso.Repository.IViviendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ViviendaService implements IViviendaService {

    @Autowired
    private IViviendaRepository viviendaRepo;

    @Override
    public List<Vivienda> listarTodas() {
        return viviendaRepo.findAll();
    }

    @Override
    public Vivienda buscarPorExpediente(String numExp) {
        if (numExp == null || numExp.trim().isEmpty()) {
            throw new IllegalArgumentException("El numero de expediente no puede estar vacío");
        }
        return viviendaRepo.findViviendaByNumExp(numExp).orElseThrow(() -> new RuntimeException("Expediente con el número " + numExp + " no encontrado."));
    }

    @Override
    public Vivienda guardar(Vivienda vivienda) {
        return viviendaRepo.save(vivienda);
    }

    @Override
    public void eliminar(String numExp) {
        if (!viviendaRepo.existsByNumExp(numExp)) {
            throw new RuntimeException("No existe expediente con el número de expediente " + numExp);
        }
        viviendaRepo.deleteViviendaByNumExp(numExp);
    }

    @Override
    public List<Vivienda> buscarPorEstado(EstadoVivienda estado) {
        return viviendaRepo.findViviendaByEstado(estado);
    }

    @Override
    public Optional<Vivienda> buscarPorFamilia(Integer idFamilia) {
        return Optional.empty();
    }
}
