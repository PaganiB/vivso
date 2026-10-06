package com.vivso.Vivso.Service;

/*import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CalendarioServiceTest {

    private final CalendarioService calendario = new CalendarioService();

    // 2026-10-09 es viernes
    @Test
    void viernesMasDosDiasVenceElMartes() {
        assertEquals(LocalDate.of(2026, 10, 13), calendario.sumarDiasHabiles(LocalDate.of(2026, 10, 9), 2));
    }

    @Test
    void sabadoMasDosDiasVenceElMartes() {
        assertEquals(LocalDate.of(2026, 10, 13), calendario.sumarDiasHabiles(LocalDate.of(2026, 10, 10), 2));
    }

    @Test
    void lunesMasDosDiasVenceElMiercoles() {
        assertEquals(LocalDate.of(2026, 10, 14), calendario.sumarDiasHabiles(LocalDate.of(2026, 10, 12), 2));
    }

    @Test
    void diezDiasHabilesSonDosSemanas() {
        assertEquals(LocalDate.of(2026, 10, 23), calendario.sumarDiasHabiles(LocalDate.of(2026, 10, 9), 10));
    }

    @Test
    void cantidadCeroDevuelveLaMismaFecha() {
        assertEquals(LocalDate.of(2026, 10, 9), calendario.sumarDiasHabiles(LocalDate.of(2026, 10, 9), 0));
    }

    @Test
    void diasNegativosLanzanExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> calendario.sumarDiasHabiles(LocalDate.of(2026, 10, 9), -1));
    }

    @Test
    void restantesDeLunesAMiercolesSonDos() {
        assertEquals(2, calendario.diasHabilesRestantes(LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 14)));
    }

    @Test
    void restantesDeViernesAMartesSonDos() {
        assertEquals(2, calendario.diasHabilesRestantes(LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 13)));
    }

    @Test
    void restantesCeroSiElLimiteEsHoyOYaPaso() {
        assertEquals(0, calendario.diasHabilesRestantes(LocalDate.of(2026, 10, 13), LocalDate.of(2026, 10, 13)));
        assertEquals(0, calendario.diasHabilesRestantes(LocalDate.of(2026, 10, 14), LocalDate.of(2026, 10, 13)));
    }
}*/