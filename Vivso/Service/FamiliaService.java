package com.vivso.Vivso.Service;

import com.vivso.Vivso.Model.Familia;
import com.vivso.Vivso.Model.IntegranteFamilia;
import com.vivso.Vivso.Repository.IFamiliaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FamiliaService implements IFamiliaService{

    @Autowired
    private IFamiliaRepository familiaRepo;

    @Override
    public List<Familia> getFamilias() {
        return familiaRepo.findAll();
    }

    @Override
    public Familia saveFamilia(Familia familia) {
        familiaRepo.save(familia);

        return familia;
    }

    @Override
    public void deleteFamilia(Integer id) {
        familiaRepo.deleteById(id);
    }

    @Override
    public Familia findFamilia(Integer id) {
        return familiaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró la familia con ID: " + id));
    }

}
