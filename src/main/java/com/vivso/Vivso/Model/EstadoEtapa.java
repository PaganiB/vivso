package com.vivso.Vivso.Model;

public enum EstadoEtapa {
    PENDIENTE,    // todavía no le toca (la anterior no está aprobada)
    EN_CURSO,     // habilitada: la ONG puede cargar fotos y observaciones
    EN_REVISION,  // la ONG la envió, espera el OK del técnico
    PRORROGADA,
    OBSERVADA,    // el técnico la devolvió con observaciones
    APROBADA      // el técnico dio el OK, habilita la siguiente
}