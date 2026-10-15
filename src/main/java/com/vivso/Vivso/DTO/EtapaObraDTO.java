package com.vivso.Vivso.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vivso.Vivso.Model.EstadoEtapa;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Respuesta: lo que ven la ONG, el técnico y las apps al consultar las etapas de un expediente.
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtapaObraDTO {

    private Integer idEtapaObra;
    private Integer idSolicitud;
    private String numExp;

    // Datos de la etapa (salen del enum EtapaConstruccion, aplanados)
    private Integer numeroEtapa;
    private String nombreEtapa;
    private String descripcionEtapa;
    private Integer diasEstimados;

    private EstadoEtapa estado;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaLimite;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaLimiteOriginal;

    private String motivoProrroga;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaEnvio;

    private Integer idUsuarioEnvio;
    private String nombreEnvio;

    private String observacionOrganizacion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaRevision;

    private Integer idUsuarioRevisor;
    private String nombreRevisor;

    private String observacionTecnico;
}