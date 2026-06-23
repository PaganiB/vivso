package com.vivso.Vivso.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudCompletaDTO {

    // === 1. OBJETOS DE TEXTO ===
    @Valid private OrganizacionDTO organizacion;
    @Valid private IntegranteDTO presidente;
    @Valid private IntegranteDTO tesorero;
    @Valid private FamiliaDTO familia;
    @Valid private SolicitudDTO solicitud;

    // === 2. ARCHIVOS FÍSICOS ===
    private MultipartFile notaSolicitud;
    private MultipartFile actaAsamblea;
    private MultipartFile constanciaCuentaBancaria;
    private MultipartFile altaAfip;
    private MultipartFile dniPresidente;
    private MultipartFile dniTesorero;

    private MultipartFile certificadoResidencia;
    private MultipartFile certificadoDiscapacidad;
    private MultipartFile escrituraPropiedad;
    private MultipartFile declaracionJurada;
    private MultipartFile actaCompromiso;

    private List<MultipartFile> dniFamiliares;
    private List<MultipartFile> fotosTerreno;
}