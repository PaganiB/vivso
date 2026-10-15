package com.vivso.Vivso.Service;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Cálculo de plazos en días hábiles (lunes a viernes).
 * Por ahora no contempla feriados: cuando se agregue la feature, el único
 * lugar a modificar es esDiaHabil().
 */
@Service
public class CalendarioService {

    public boolean esDiaHabil(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            return false;
        }
        // TODO feriados: return !feriadoRepository.existsByFecha(fecha);
        return true;
    }

    /**
     * Fecha de vencimiento: el día de "desde" no cuenta, el día 1 es el siguiente día hábil.
     * Ej: habilitada un viernes con 2 días -> vence el martes (lunes = día 1, martes = día 2).
     */
    public LocalDate sumarDiasHabiles(LocalDate desde, int dias) {
        if (dias < 0) {
            throw new IllegalArgumentException("Los días no pueden ser negativos");
        }
        LocalDate fecha = desde;
        int restantes = dias;
        while (restantes > 0) {
            fecha = fecha.plusDays(1);
            if (esDiaHabil(fecha)) {
                restantes--;
            }
        }
        return fecha;
    }

    /**
     * Días hábiles que faltan para "limite", sin contar "hoy" y contando el día del límite.
     * Devuelve 0 si el límite es hoy o ya pasó (el vencimiento se detecta con hoy.isAfter(limite)).
     */
    public int diasHabilesRestantes(LocalDate hoy, LocalDate limite) {
        int cantidad = 0;
        LocalDate fecha = hoy;
        while (fecha.isBefore(limite)) {
            fecha = fecha.plusDays(1);
            if (esDiaHabil(fecha)) {
                cantidad++;
            }
        }
        return cantidad;
    }
}