package com.vivso.Vivso.DTO;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ExpedienteDetalleDTO {

    private Integer id;
    private String numero;
    private String estado;
    private Beneficiario beneficiario;
    private Ong ong;
    private Org org;
    private Autoridad presidente;
    private Autoridad tesorero;
    private SolicitudDatos solicitud;
    private List<VisitaInicialDTO> visitas;      // Historial, sección 1 (visita inicial)

    // Bloques nuevos: el front todavía no los tiene en TExpediente
    private ViviendaDTO vivienda;                // null hasta que se aprueba
    private List<EtapaConFotos> etapas;          // Historial, sección 2

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Beneficiario { private String nombre; private String dni; }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Ong { private String nombre; private String cuit; }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Org {
        private String cuit;
        private String nombre;
        private String tipo;
        private String domicilioLegal;
        private String nroCpe;
        private LocalDate vencimientoVigencia;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Autoridad {
        private String nombre;
        private String apellido;
        private String dni;
        private String email;
        private String telefono;
        private String domicilio;
        private LocalDate fechaAltaCargo;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SolicitudDatos {
        private String representanteNombre;
        private String representanteTelefono;
        private LocalDate fechaSolicitud;
        private String observaciones;
        private List<MiembroFamilia> grupoFamiliar;
        private Integer antiguedadPosesion;
        private String ubicacion;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MiembroFamilia {
        private String dni;
        private String nombre;
        private String apellido;
        private boolean discapacidad;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class EtapaConFotos {
        private EtapaObraDTO etapa;
        private List<FotoEtapaDTO> fotos;
    }
}
