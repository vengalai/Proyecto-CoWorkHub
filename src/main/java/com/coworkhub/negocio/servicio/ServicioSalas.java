package com.coworkhub.negocio.servicio;

import com.coworkhub.presentacion.dto.RespuestaSala;
import com.coworkhub.presentacion.dto.SolicitudSala;

import java.util.List;

public interface ServicioSalas {

    List<RespuestaSala> listar(Long sedeId);

    RespuestaSala buscar(Long id);

    RespuestaSala crear(SolicitudSala solicitud);

    RespuestaSala actualizar(Long id, SolicitudSala solicitud);

    void eliminar(Long id);
}
