package com.vivso.Vivso.Model;

public enum EtapaConstruccion {

    ETAPA_1(1, "Replanteo y cimientos",
            "Replanteo, excavación y llenado de cimientos", 2),
    ETAPA_2(2, "Mampostería de cimientos",
            "Mampostería de cimientos, encadenado inferior c/tronco de columna y capa aisladora", 2),
    ETAPA_3(3, "Elevación y techo",
            "Mampostería de elevación, columnas de encadenado, encadenado superior, techo completo c/aislante, apretada y colocación de aberturas", 10),
    ETAPA_4(4, "Revoques y contrapiso",
            "Revoque grueso y fino interior y exterior, contrapiso y carpeta de nivelación", 8),
    ETAPA_5(5, "Instalaciones",
            "Instalación eléctrica, instalación sanitaria, colocación de mesada, cámaras y pozo", 5),
    ETAPA_6(6, "Pintura y artefactos",
            "Pintura interior y exterior, colocación de artefactos eléctricos, colocación de artefactos sanitarios", 2);

    private final int numero;
    private final String nombre;
    private final String descripcion;
    private final int diasEstimados;

    EtapaConstruccion(int numero, String nombre, String descripcion, int diasEstimados) {
        this.numero = numero;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.diasEstimados = diasEstimados;
    }

    public int getNumero() {
        return numero;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getDiasEstimados() {
        return diasEstimados;
    }

    public static EtapaConstruccion primera() {
        return ETAPA_1;
    }

    // Devuelve la etapa que sigue, o null si ya es la última (obra finalizada).
    public EtapaConstruccion obtenerSiguienteEtapa() {
        for (EtapaConstruccion e : values()) {
            if (e.numero == this.numero + 1) {
                return e;
            }
        }
        return null;
    }
}

