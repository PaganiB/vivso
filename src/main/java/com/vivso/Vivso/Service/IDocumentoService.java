package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.DocumentoDTO;
import com.vivso.Vivso.Model.Organizacion;
import com.vivso.Vivso.Model.TipoDocumento;
import com.vivso.Vivso.Model.Usuario;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IDocumentoService {

    DocumentoDTO subirDocumento(MultipartFile archivo, TipoDocumento tipo, Integer idFamilia, String cuitOrg);
    DocumentoDTO subirDocumento(MultipartFile archivo, TipoDocumento tipo, Integer idFamilia, String cuitOrg, Integer idFamiliar);
    DocumentoDTO marcarParaCorregir(Integer idDoc, String motivo);
    DocumentoDTO reemplazar(Integer idDoc, MultipartFile archivo);
    List<DocumentoDTO> listarPorFamilia(Integer idFamilia);
    List<DocumentoDTO> listarPorOrganizacion(String cuitOrg);
    List<DocumentoDTO> listarPorUsuarioRevisor(Integer idUsuario);
    void eliminar(Integer id);

    // Hacen lo mismo que el subir documento pero en 2 partes, porque en solicitud organizacion es asi
    String guardarArchivoFisico(MultipartFile archivo);
    void borrarArchivoFisico(String url);

    // Metodos que se usan para el formulario de organizacion
    DocumentoDTO subirDocumentoDeSolicitudOrg(MultipartFile archivo, TipoDocumento tipo, Integer idSolicitudOrg);
    List<DocumentoDTO> listarPorSolicitudOrg(Integer idSolicitudOrg);
    void aprobarConOrganizacion(Integer idDocumento, Organizacion org, Usuario revisor);
    void rechazar(Integer idDocumento, Usuario revisor);

}
