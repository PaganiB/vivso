package com.vivso.Vivso.DTO;

import com.vivso.Vivso.Model.EstadoDocumento;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RevisionDocumentoDTO {
    private Integer idRevisor;
    private EstadoDocumento nuevoEstado;
    private String motivo;
}