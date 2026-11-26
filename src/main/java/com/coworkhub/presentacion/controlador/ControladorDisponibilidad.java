package com.coworkhub.presentacion.controlador;

import com.coworkhub.negocio.servicio.ServicioDisponibilidad;
import com.coworkhub.presentacion.dto.RespuestaSala;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/disponibilidad")
@Tag(name = "Disponibilidad", description = "Consulta de salas libres")
public class ControladorDisponibilidad {

    private final ServicioDisponibilidad servicioDisponibilidad;

    public ControladorDisponibilidad(ServicioDisponibilidad servicioDisponibilidad) {
        this.servicioDisponibilidad = servicioDisponibilidad;
    }

    @GetMapping
    @Operation(summary = "Salas libres de una sede en una fecha y rango horario")
    public List<RespuestaSala> consultar(
            @RequestParam Long sedeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFin,
            @RequestParam(defaultValue = "1") int capacidadMinima,
            @RequestParam(required = false) List<Long> equipamientoIds) {
        return servicioDisponibilidad.consultar(sedeId, fecha, horaInicio, horaFin,
                capacidadMinima, equipamientoIds);
    }
}
