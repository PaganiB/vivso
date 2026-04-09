package com.vivso.Vivso.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FamiliaDTO {

    private Integer idFamilia;

    @NotBlank(message = "El nombre del representante es obligatorio")
    private String nombreRepresentante;

    @NotBlank(message = "Debe proporcionar un número de contacto")
    private String contacto;

    @NotNull(message = "La antigüedad del rancho es obligatoria")
    @Min(value = 5, message = "La antigüedad del rancho debe ser de al menos 5 años para calificar")
    private Integer antiguedadRancho;
    @NotBlank(message = "Debe proporcionar las coordenadas de la vivienda")
    private String coordenadasRancho;

    @NotBlank(message = "El CUIT de la organización es obligatorio para vincular la familia")
    private String cuitOrg;

    @NotBlank(message = "El acta de compromiso es obligatoria para el alta de la familia")
    @Pattern(
            regexp = "(?i).*\\.(pdf|jpg|png|jpeg)$",
            message = "Formato no válido. Solo se permiten archivos PDF, JPG o PNG"
    )
    private String actaCompromisoUrl;
    @NotBlank(message = "Debe adjuntar el certificado de residencia emitido por la policía")
    @Pattern(
            regexp = "(?i).*\\.(pdf|jpg|png|jpeg)$",
            message = "Formato no válido. Solo se permiten archivos PDF, JPG o PNG"
    )
    private String certificadoResidenciaUrl;
    @NotBlank(message = "La copia de la escritura es necesaria")
    @Pattern(
            regexp = "(?i).*\\.(pdf|jpg|png|jpeg)$",
            message = "Formato no válido. Solo se permiten archivos PDF, JPG o PNG"
    )
    private String escrituraUrl;
    @NotBlank(message = "La declaración jurada de ingresos debe estar firmada y cargada")
    @Pattern(
            regexp = "(?i).*\\.(pdf|jpg|png|jpeg)$",
            message = "Formato no válido. Solo se permiten archivos PDF, JPG o PNG"
    )
    private String declaracionJuradaUrl;
    @NotBlank(message = "La foto de la vivienda es obligatoria para el relevamiento")
    @Pattern(
            regexp = "(?i).*\\.(pdf|jpg|png|jpeg)$",
            message = "Formato no válido. Solo se permiten archivos PDF, JPG o PNG"
    )
    private String fotoViviendaUrl;

}
