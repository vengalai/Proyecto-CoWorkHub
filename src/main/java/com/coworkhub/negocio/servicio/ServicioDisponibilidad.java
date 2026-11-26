package com.coworkhub.negocio.servicio;

import com.coworkhub.presentacion.dto.RespuestaSala;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ServicioDisponibilidad {

    List<RespuestaSala> consultar(Long sedeId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                  int capacidadMinima, List<Long> equipamientoIds);
}
