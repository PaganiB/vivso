package com.vivso.Vivso.Service;

import com.vivso.Vivso.Model.Familia;

import java.util.List;

public interface IFamiliaService {

    public List<Familia> getFamilias();

    public Familia saveFamilia(Familia familia);

    public void deleteFamilia(Integer id);

    public Familia findFamilia(Integer id);

}
