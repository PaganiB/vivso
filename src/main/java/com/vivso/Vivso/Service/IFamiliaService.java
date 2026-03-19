package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.FamiliaDTO;
import com.vivso.Vivso.Model.Familia;

import java.util.List;

public interface IFamiliaService {

    public List<FamiliaDTO> getFamilias();

    public FamiliaDTO saveFamilia(FamiliaDTO familiaDto);

    public FamiliaDTO updateFamilia(Integer idFamilia, FamiliaDTO familiaDto);

    public void deleteFamilia(Integer id);

    public FamiliaDTO findFamilia(Integer id);

}
