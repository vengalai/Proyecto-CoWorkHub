package com.coworkhub.negocio.excepcion;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccesoDenegadoException extends ExcepcionNegocio {

    public AccesoDenegadoException(String mensaje) {
        super("ACCESO_DENEGADO", mensaje);
    }
}
