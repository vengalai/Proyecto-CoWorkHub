package com.coworkhub.seguridad;

import com.coworkhub.presentacion.dto.RespuestaLogin;
import com.coworkhub.presentacion.dto.RespuestaPerfil;
import com.coworkhub.presentacion.dto.SolicitudLogin;

public interface ServicioAutenticacion {

    RespuestaLogin iniciarSesion(SolicitudLogin solicitud);

    RespuestaPerfil perfil();
}
