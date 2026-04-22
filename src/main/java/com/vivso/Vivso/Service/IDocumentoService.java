package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.DocumentoDTO;
import com.vivso.Vivso.Model.EstadoDocumento;
import com.vivso.Vivso.Model.TipoDocumento;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IDocumentoService {

    DocumentoDTO subirDocumento(MultipartFile archivo, TipoDocumento tipo, Integer idFamilia, String cuitOrg);
    DocumentoDTO revisar(Integer idDoc, Integer idRevisor, EstadoDocumento nuevoEstado, String motivo);
    List<DocumentoDTO> listarPorFamilia(Integer idFamilia);
    List<DocumentoDTO> listarPorOrganizacion(String cuitOrg);
    List<DocumentoDTO> listarPorUsuarioRevisor(Integer idUsuario);
    void eliminar(Integer id);
}
