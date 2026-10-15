package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.VisitaObraDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IVisitaObraService {

    VisitaObraDTO registrarVisita(VisitaObraDTO dto);

    List<VisitaObraDTO> obtenerHistorialPorVivienda(Integer idVivienda);

    void registrarAvanceDeObra(Integer idSolicitud, Integer avanceFisico, String observaciones, List<MultipartFile> fotos);
}