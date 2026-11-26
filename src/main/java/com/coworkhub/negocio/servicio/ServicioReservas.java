package com.coworkhub.negocio.servicio;

import com.coworkhub.negocio.model.EstadoReserva;
import com.coworkhub.presentacion.dto.ItemServicio;
import com.coworkhub.presentacion.dto.RespuestaReserva;
import com.coworkhub.presentacion.dto.SolicitudReserva;

import java.time.LocalDate;
import java.util.List;

public interface ServicioReservas {

    RespuestaReserva crear(SolicitudReserva solicitud);

    RespuestaReserva buscar(Long id);

    List<RespuestaReserva> listar(Long miembroId, Long salaId, LocalDate fecha, EstadoReserva estado);

    RespuestaReserva agregarServicio(Long reservaId, ItemServicio item);

    RespuestaReserva quitarServicio(Long reservaId, Long servicioId);

    RespuestaReserva confirmar(Long id);

    RespuestaReserva cancelar(Long id);

    RespuestaReserva completar(Long id);
}
