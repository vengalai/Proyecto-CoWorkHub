package com.coworkhub.negocio.servicio;

import com.coworkhub.presentacion.dto.RespuestaPlan;
import com.coworkhub.presentacion.dto.SolicitudPlan;

import java.util.List;

public interface ServicioPlanes {

    List<RespuestaPlan> listar();

    RespuestaPlan buscar(Long id);

    RespuestaPlan crear(SolicitudPlan solicitud);

    RespuestaPlan actualizar(Long id, SolicitudPlan solicitud);

    void eliminar(Long id);
}
