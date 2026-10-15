package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.EnviarEtapaDTO;
import com.vivso.Vivso.DTO.EtapaObraDTO;
import com.vivso.Vivso.DTO.ObservarEtapaDTO;
import com.vivso.Vivso.DTO.ProrrogaEtapaDTO;
import com.vivso.Vivso.Service.IEtapaObraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/etapa")
@RequiredArgsConstructor
public class EtapaObraController {

    private final IEtapaObraService etapaObraService;

    // ======================== LISTAR ========================
    @GetMapping("/solicitud/{idSolicitud}")
    public ResponseEntity<List<EtapaObraDTO>> listarEtapas(@PathVariable Integer idSolicitud, Authentication authentication) {
        String email = authentication.getName();
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<EtapaObraDTO> etapas = etapaObraService.listarEtapas(idSolicitud, email, esAdmin);
        return ResponseEntity.ok(etapas);
    }

    // ======================== INICIAR OBRA ========================
    @PostMapping("/solicitud/{idSolicitud}/iniciar")
    public ResponseEntity<List<EtapaObraDTO>> iniciarObra(@PathVariable Integer idSolicitud, Authentication authentication) {
        String emailTecnico = authentication.getName();
        List<EtapaObraDTO> etapas = etapaObraService.iniciarObra(idSolicitud, emailTecnico);
        return ResponseEntity.ok(etapas);
    }

    // ======================== ENVIAR (ONG) ========================
    @PostMapping("/{idEtapa}/enviar")
    public ResponseEntity<EtapaObraDTO> enviarEtapa(
            @PathVariable Integer idEtapa,
            @RequestBody(required = false) @Valid EnviarEtapaDTO dto,
            Authentication authentication) {

        String email = authentication.getName();
        EtapaObraDTO etapaActualizada = etapaObraService.enviarEtapa(idEtapa, dto, email);
        return ResponseEntity.ok(etapaActualizada);
    }

    // ======================== APROBAR (TÉCNICO) ========================
    @PostMapping("/{idEtapa}/aprobar")
    public ResponseEntity<EtapaObraDTO> aprobarEtapa(@PathVariable Integer idEtapa, Authentication authentication) {
        String emailTecnico = authentication.getName();
        EtapaObraDTO etapaActualizada = etapaObraService.aprobarEtapa(idEtapa, emailTecnico);
        return ResponseEntity.ok(etapaActualizada);
    }

    // ======================== OBSERVAR (TÉCNICO) ========================
    @PostMapping("/{idEtapa}/observar")
    public ResponseEntity<EtapaObraDTO> observarEtapa(
            @PathVariable Integer idEtapa,
            @RequestBody @Valid ObservarEtapaDTO dto,
            Authentication authentication) {

        String emailTecnico = authentication.getName();
        EtapaObraDTO etapaActualizada = etapaObraService.observarEtapa(idEtapa, dto, emailTecnico);
        return ResponseEntity.ok(etapaActualizada);
    }

    // ======================== PRORROGAR (TÉCNICO) ========================
    @PostMapping("/{idEtapa}/prorroga")
    public ResponseEntity<EtapaObraDTO> prorrogarEtapa(
            @PathVariable Integer idEtapa,
            @RequestBody @Valid ProrrogaEtapaDTO dto,
            Authentication authentication) {

        // Extraemos la identidad del técnico directamente del token JWT por seguridad
        String emailTecnico = authentication.getName();

        // Llamamos al motor del Service que valida el tope de 14 días y que no caiga en fin de semana
        EtapaObraDTO etapaActualizada = etapaObraService.prorrogarEtapa(idEtapa, dto, emailTecnico);

        return ResponseEntity.ok(etapaActualizada);
    }
}