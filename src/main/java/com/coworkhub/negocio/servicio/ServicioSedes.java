package com.coworkhub.negocio.servicio;

import com.coworkhub.presentacion.dto.RespuestaSede;
import com.coworkhub.presentacion.dto.SolicitudSede;

import java.util.List;

public interface ServicioSedes {

    List<RespuestaSede> listar();

    RespuestaSede buscar(Long id);

    RespuestaSede crear(SolicitudSede solicitud);

    RespuestaSede actualizar(Long id, SolicitudSede solicitud);

    void eliminar(Long id);
}
