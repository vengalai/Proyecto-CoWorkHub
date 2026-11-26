package com.coworkhub.negocio.implementacion;

import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.SolicitudInvalidaException;
import com.coworkhub.negocio.model.Equipamiento;
import com.coworkhub.negocio.model.EstadoReserva;
import com.coworkhub.negocio.model.Sala;
import com.coworkhub.negocio.model.Sede;
import com.coworkhub.negocio.servicio.ServicioDisponibilidad;
import com.coworkhub.persistencia.repositorio.RepositorioSalas;
import com.coworkhub.persistencia.repositorio.RepositorioSedes;
import com.coworkhub.presentacion.dto.RespuestaSala;
import com.coworkhub.presentacion.mapper.Mapeador;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ServicioDisponibilidadImpl implements ServicioDisponibilidad {

    private static final List<EstadoReserva> ESTADOS_ACTIVOS =
            List.of(EstadoReserva.PENDING, EstadoReserva.CONFIRMED);

    private final RepositorioSedes repositorioSedes;
    private final RepositorioSalas repositorioSalas;
    private final Mapeador mapeador;

    public ServicioDisponibilidadImpl(RepositorioSedes repositorioSedes, RepositorioSalas repositorioSalas,
                                      Mapeador mapeador) {
        this.repositorioSedes = repositorioSedes;
        this.repositorioSalas = repositorioSalas;
        this.mapeador = mapeador;
    }

    @Override
    public List<RespuestaSala> consultar(Long sedeId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                         int capacidadMinima, List<Long> equipamientoIds) {
        if (fecha == null || horaInicio == null || horaFin == null) {
            throw new SolicitudInvalidaException(
                    "Los parametros 'fecha', 'horaInicio' y 'horaFin' son obligatorios");
        }
        if (!horaInicio.isBefore(horaFin)) {
            throw new SolicitudInvalidaException("'horaInicio' debe ser anterior a 'horaFin'");
        }
        if (capacidadMinima < 1) {
            throw new SolicitudInvalidaException("'capacidadMinima' debe ser mayor que cero");
        }
        Sede sede = repositorioSedes.findById(sedeId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sede", sedeId));

        LocalDateTime inicio = LocalDateTime.of(fecha, horaInicio);
        LocalDateTime fin = LocalDateTime.of(fecha, horaFin);
        if (horaInicio.isBefore(sede.getHoraApertura()) || horaFin.isAfter(sede.getHoraCierre())) {
            return List.of();
        }

        Set<Long> requeridos = equipamientoIds == null ? Set.of() : new HashSet<>(equipamientoIds);
        List<Sala> disponibles = repositorioSalas.buscarDisponibles(sedeId, capacidadMinima, inicio, fin,
                ESTADOS_ACTIVOS);
        return disponibles.stream()
                .filter(sala -> {
                    Set<Long> ids = sala.getEquipamientos().stream()
                            .map(Equipamiento::getId)
                            .collect(Collectors.toSet());
                    return ids.containsAll(requeridos);
                })
                .map(mapeador::aRespuesta)
                .toList();
    }
}
