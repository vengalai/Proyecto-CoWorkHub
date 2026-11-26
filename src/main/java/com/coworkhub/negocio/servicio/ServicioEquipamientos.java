package com.coworkhub.negocio.servicio;

import com.coworkhub.presentacion.dto.RespuestaEquipamiento;
import com.coworkhub.presentacion.dto.SolicitudEquipamiento;

import java.util.List;

public interface ServicioEquipamientos {

    List<RespuestaEquipamiento> listar();

    RespuestaEquipamiento buscar(Long id);

    RespuestaEquipamiento crear(SolicitudEquipamiento solicitud);

    RespuestaEquipamiento actualizar(Long id, SolicitudEquipamiento solicitud);

    void eliminar(Long id);
}
