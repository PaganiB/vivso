package com.vivso.Vivso.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class FotoEtapaDTO {
    private Long idFoto;
    private Integer idEtapa;
    private String rutaArchivo;
    private String nombreArchivo;

  @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime fechaSubida;

    private Integer idUsuarioSubida;
    private String nombreUsuarioSubida;
}