package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.FamiliaDTO;
import com.vivso.Vivso.Model.Familia;

import java.util.List;

public interface IFamiliaService {

    List<FamiliaDTO> getFamilias();

    FamiliaDTO saveFamilia(FamiliaDTO familiaDto);

    FamiliaDTO updateFamilia(Integer idFamilia, FamiliaDTO familiaDto);

    void deleteFamilia(Integer id);

    FamiliaDTO findFamilia(Integer id);

}
