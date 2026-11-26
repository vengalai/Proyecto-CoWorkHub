package com.coworkhub.negocio.excepcion;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class NoAutenticadoException extends ExcepcionNegocio {

    public NoAutenticadoException(String mensaje) {
        super("NO_AUTENTICADO", mensaje);
    }
}
