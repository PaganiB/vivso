package com.vivso.Vivso.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrganizacionDTO {

    @NotBlank(message = "El CUIT es obligatorio")
    @Pattern(regexp = "^(30|33)-\\d{8}-\\d{1}$", message = "El CUIT debe tener el formato XX-XXXXXXXX-X, solo se admiten cuit de empresas 30 o 33")
    private String cuit;

    @NotBlank(message = "El nombre de la organización es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "Especifique el tipo de organización (Ej: ONG, Asociación, Cooperativa)")
    private String tipo;

    @NotBlank(message = "El domicilio legal es obligatorio para notificaciones")
    private String dom_legal;

    @NotBlank(message = "Debe proporcionar un teléfono o email de contacto")
    private String contacto;

    @NotBlank(message = "El Código Postal (CPE) es obligatorio")
    private String cpe;

    // --- VALIDACIÓN DE DOCUMENTACIÓN (PDF o Imágenes) ---

    @NotBlank(message = "La nota de solicitud es obligatoria")
    @Pattern(regexp = "(?i).*\\.(pdf|jpg|png|jpeg)$", message = "La nota de solicitud debe ser PDF, JPG o PNG")
    private String nota_solicitud_url;

    @NotBlank(message = "El certificado de vigencia es obligatorio")
    @Pattern(regexp = "(?i).*\\.(pdf|jpg|png|jpeg)$", message = "La vigencia debe ser PDF, JPG o PNG")
    private String vigencia_url;

    @NotBlank(message = "El acta de compromiso es obligatoria")
    @Pattern(regexp = "(?i).*\\.(pdf|jpg|png|jpeg)$", message = "El acta de compromiso debe ser PDF, JPG o PNG")
    private String acta_compromiso_url;

    @NotBlank(message = "El acta de asamblea es obligatoria")
    @Pattern(regexp = "(?i).*\\.(pdf|jpg|png|jpeg)$", message = "El acta de asamblea debe ser PDF, JPG o PNG")
    private String acta_asamblea_url;

    @NotBlank(message = "Debe adjuntar el DNI de las autoridades")
    @Pattern(regexp = "(?i).*\\.(pdf|jpg|png|jpeg)$", message = "El DNI de autoridades debe ser PDF, JPG o PNG")
    private String dni_autoridades_url;

    @NotBlank(message = "El certificado de residencia de la sede es obligatorio")
    @Pattern(regexp = "(?i).*\\.(pdf|jpg|png|jpeg)$", message = "El certificado de residencia debe ser PDF, JPG o PNG")
    private String certificado_residencia_url;

    @NotBlank(message = "La constancia de cuenta bancaria es obligatoria")
    @Pattern(regexp = "(?i).*\\.(pdf|jpg|png|jpeg)$", message = "La constancia bancaria debe ser PDF, JPG o PNG")
    private String cuenta_bancaria_url;

    @NotBlank(message = "La constancia de alta en AFIP es obligatoria")
    @Pattern(regexp = "(?i).*\\.(pdf|jpg|png|jpeg)$", message = "El alta de AFIP debe ser PDF, JPG o PNG")
    private String alta_afip_url;
}
