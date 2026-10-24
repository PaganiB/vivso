package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.*;
import jakarta.validation.Valid;

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

    EtapaObraDTO rechazarEtapa(Integer idEtapa, RechazarEtapaDTO dto, String emailTecnico);
}
