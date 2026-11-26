package com.coworkhub.negocio.servicio;

import com.coworkhub.negocio.model.EstadoMiembro;
import com.coworkhub.presentacion.dto.Pagina;
import com.coworkhub.presentacion.dto.RespuestaMiembro;
import com.coworkhub.presentacion.dto.ResumenConsumo;
import com.coworkhub.presentacion.dto.SolicitudActualizarMiembro;
import com.coworkhub.presentacion.dto.SolicitudMiembro;

public interface ServicioMiembros {

    Pagina<RespuestaMiembro> listar(String nombre, EstadoMiembro estado, int pagina, int tamano, String orden);

    RespuestaMiembro buscar(Long id);

    RespuestaMiembro crear(SolicitudMiembro solicitud);

    RespuestaMiembro actualizar(Long id, SolicitudActualizarMiembro solicitud);

    RespuestaMiembro suspender(Long id);

    RespuestaMiembro activar(Long id);

    void eliminar(Long id);

    ResumenConsumo consumo(Long id, String mes);
}
