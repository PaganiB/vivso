package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.VisitaHistorialDTO;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Model.VisitaInicial;
import com.vivso.Vivso.Model.VisitaObra;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import com.vivso.Vivso.Repository.IVisitaInicialRepository;
import com.vivso.Vivso.Repository.IVisitaObraRepository;
import com.vivso.Vivso.Exception.RecursoNoEncontradoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class HistorialService {

    @Autowired private IVisitaInicialRepository visitaInicialRepo;
    @Autowired private IVisitaObraRepository visitaObraRepo;
    @Autowired private IUsuarioRepository usuarioRepo;

    public List<VisitaHistorialDTO> obtenerHistorialGeneral() {
        // 1. Identificamos al técnico autenticado mediante el Token JWT
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario tecnico = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Técnico no encontrado"));

        List<VisitaHistorialDTO> historialUnificado = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // 2. Buscamos y mapeamos las Visitas Iniciales del técnico
        List<VisitaInicial> iniciales = visitaInicialRepo.findByTecnico_Id(tecnico.getId());

        List<VisitaHistorialDTO> dtoIniciales = iniciales.stream().map(v ->
                VisitaHistorialDTO.builder()
                        .id("inicial_" + v.getId())
                        .tipo("INICIAL")
                        .expediente(v.getSolicitud() != null ? v.getSolicitud().getNumExp() : "S/N")
                        .localidad(v.getLocalidad() != null ? v.getLocalidad() : "Sin localidad")
                        .departamento(v.getDepartamento() != null ? v.getDepartamento() : "Sin depto")
                        .fecha(v.getFechaVisita() != null ? v.getFechaVisita().format(formatter) : "")
                        .clasificacion(v.getClasificacionPreliminar() != null ? v.getClasificacionPreliminar().name() : "-")
                        .estado("sincronizada")
                        .avanceActual(0)
                        // 👇 Asignamos los datos reales al DTO
                        .cantHabitantesReal(v.getCantHabitantesReal())
                        .tienePrioridadSocial(v.getTienePrioridadSocial())
                        .observaciones(v.getObservaciones())
                        .lat(v.getLat())
                        .lng(v.getLng())
                        .urlsFotos(v.getUrlsFotos())
                        .tecnicoEmail(v.getTecnico() != null ? v.getTecnico().getEmail() : "Sin técnico")
                        .build()
        ).toList();

        historialUnificado.addAll(dtoIniciales);

        // 3. Buscamos y mapeamos las Visitas de Obra del técnico
        List<VisitaObra> obras = visitaObraRepo.findByTecnico_Id(tecnico.getId());

        List<VisitaHistorialDTO> dtoObras = obras.stream().map(o ->
                VisitaHistorialDTO.builder()
                        .id("obra_" + o.getIdVisitaObra())
                        .tipo("OBRA")
                        .expediente(o.getVivienda() != null ? "Vivienda #" + o.getVivienda().getIdVivienda() : "S/N")
                        .localidad(o.getVivienda() != null ? o.getVivienda().getLocalidad() : "-")
                        .departamento(o.getVivienda() != null ? o.getVivienda().getDepartamento() : "-")
                        .fecha(o.getFecha() != null ? o.getFecha().format(formatter) : "")
                        .clasificacion("-")
                        .estado("sincronizada")
                        .avanceActual(o.getAfo() != null ? o.getAfo() : 0)
                        .build()
        ).toList();

        historialUnificado.addAll(dtoObras);

        // 4. Ordenamos por fecha descendiente (lo más nuevo arriba)
        // Opcional, pero le da un toque muy profesional a la app de Feli.

        return historialUnificado;
    }
}