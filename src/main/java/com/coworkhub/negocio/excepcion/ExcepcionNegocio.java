package com.coworkhub.negocio.excepcion;

/**
 * Base de las excepciones de negocio. Cada subclase declara su codigo estable
 * (el que ve el cliente) y su estado HTTP con @ResponseStatus.
 */
public abstract class ExcepcionNegocio extends RuntimeException {

    private final String codigo;

    protected ExcepcionNegocio(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
