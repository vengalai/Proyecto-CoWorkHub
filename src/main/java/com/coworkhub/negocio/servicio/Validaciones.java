package com.coworkhub.negocio.servicio;

import com.coworkhub.negocio.excepcion.SolicitudInvalidaException;

import java.math.BigDecimal;

/**
 * Validaciones de forma que Bean Validation no puede expresar (por ejemplo,
 * comparar dos campos entre si). Las reglas de negocio RN-xx viven en cada servicio.
 */
public final class Validaciones {

    private Validaciones() {
    }

    public static String texto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new SolicitudInvalidaException("El campo '" + campo + "' es obligatorio");
        }
        return valor.trim();
    }

    public static void montoPositivo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() <= 0) {
            throw new SolicitudInvalidaException("El campo '" + campo + "' debe ser mayor que cero");
        }
    }

    public static void rango(int valor, int minimo, int maximo, String campo) {
        if (valor < minimo || valor > maximo) {
            throw new SolicitudInvalidaException("El campo '" + campo + "' debe estar entre "
                    + minimo + " y " + maximo);
        }
    }
}
