package com.coworkhub.negocio.excepcion;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ReglaNegocioException extends ExcepcionNegocio {

    public ReglaNegocioException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
