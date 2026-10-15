package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.FotoEtapaDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface IFotoEtapaService {
    List<FotoEtapaDTO> subirFotos(Integer idEtapa, List<MultipartFile> archivos, String email);
    List<FotoEtapaDTO> listarFotosPorEtapa(Integer idEtapa, String email, boolean esAdmin);
    void eliminarFoto(Long idFoto, String email);
}
