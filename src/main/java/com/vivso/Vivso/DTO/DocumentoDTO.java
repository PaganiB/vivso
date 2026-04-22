package com.vivso.Vivso.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vivso.Vivso.Model.EstadoDocumento;
import com.vivso.Vivso.Model.TipoDocumento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoDTO {

    private Integer idDocumento; // Para cuando consultamos

    @NotBlank(message = "El nombre del documento es obligatorio")
    private String nombre;

    private String url; // La ruta que devuelve el servidor

    @NotNull(message = "El tipo de documento es obligatorio")
    private TipoDocumento tipo;

    private EstadoDocumento estado;

    private String motivoRechazo;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime fechaSubida;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime fechaRevision;

    // --- Referencias por ID ---
    // Usamos IDs para que el JSON sea liviano

    private Integer id_familia;

    private String cuitOrg;

    private Integer idUsuarioRevisor;

    private String nombreRevisor; // Opcional: para mostrar quién lo revisó sin buscar el ID
}
