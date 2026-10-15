package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.EnviarEtapaDTO;
import com.vivso.Vivso.DTO.EtapaObraDTO;
import com.vivso.Vivso.DTO.ObservarEtapaDTO;
import com.vivso.Vivso.DTO.ProrrogaEtapaDTO;

import java.util.List;

public interface IEtapaObraService {

    // Iniciar
    List<EtapaObraDTO> iniciarObra(Integer idSolicitud, String emailTecnico);

    // Listar
    List<EtapaObraDTO> listarEtapas(Integer idSolicitud, String email, boolean esAdmin);

    // Acciones de estado
    EtapaObraDTO enviarEtapa(Integer idEtapa, EnviarEtapaDTO dto, String email);

    EtapaObraDTO aprobarEtapa(Integer idEtapa, String email);

    EtapaObraDTO observarEtapa(Integer idEtapa, ObservarEtapaDTO dto, String email);

    EtapaObraDTO prorrogarEtapa(Integer idEtapa, ProrrogaEtapaDTO dto, String email);

}
