package com.coworkhub.negocio.excepcion;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SolicitudInvalidaException extends ExcepcionNegocio {

    public SolicitudInvalidaException(String mensaje) {
        super("SOLICITUD_INVALIDA", mensaje);
    }
}
