package com.coworkhub.negocio.excepcion;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecursoNoEncontradoException extends ExcepcionNegocio {

    public RecursoNoEncontradoException(String mensaje) {
        super("NO_ENCONTRADO", mensaje);
    }

    public RecursoNoEncontradoException(String recurso, Long id) {
        this(recurso + " con id " + id + " no existe");
    }
}
