package com.coworkhub.negocio.servicio;

import com.coworkhub.presentacion.dto.RespuestaServicioAdicional;
import com.coworkhub.presentacion.dto.SolicitudServicioAdicional;

import java.util.List;

public interface ServicioServiciosAdicionales {

    List<RespuestaServicioAdicional> listar();

    RespuestaServicioAdicional buscar(Long id);

    RespuestaServicioAdicional crear(SolicitudServicioAdicional solicitud);

    RespuestaServicioAdicional actualizar(Long id, SolicitudServicioAdicional solicitud);

    void eliminar(Long id);
}
