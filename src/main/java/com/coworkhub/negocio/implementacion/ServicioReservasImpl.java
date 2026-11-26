package com.coworkhub.negocio.implementacion;

import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.excepcion.SolicitudInvalidaException;
import com.coworkhub.negocio.model.DetalleReserva;
import com.coworkhub.negocio.model.EstadoMiembro;
import com.coworkhub.negocio.model.EstadoReserva;
import com.coworkhub.negocio.model.Miembro;
import com.coworkhub.negocio.model.Reserva;
import com.coworkhub.negocio.model.Sala;
import com.coworkhub.negocio.model.ServicioAdicional;
import com.coworkhub.negocio.servicio.CalculadoraCostoReserva;
import com.coworkhub.negocio.servicio.ServicioReservas;
import com.coworkhub.persistencia.repositorio.RepositorioMiembros;
import com.coworkhub.persistencia.repositorio.RepositorioReservas;
import com.coworkhub.persistencia.repositorio.RepositorioSalas;
import com.coworkhub.persistencia.repositorio.RepositorioServiciosAdicionales;
import com.coworkhub.presentacion.dto.ItemServicio;
import com.coworkhub.presentacion.dto.RespuestaReserva;
import com.coworkhub.presentacion.dto.SolicitudReserva;
import com.coworkhub.presentacion.mapper.Mapeador;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ServicioReservasImpl implements ServicioReservas {

    private static final List<EstadoReserva> ESTADOS_ACTIVOS =
            List.of(EstadoReserva.PENDING, EstadoReserva.CONFIRMED);

    private final RepositorioReservas repositorioReservas;
    private final RepositorioSalas repositorioSalas;
    private final RepositorioMiembros repositorioMiembros;
    private final RepositorioServiciosAdicionales repositorioServicios;
    private final CalculadoraCostoReserva calculadora;
    private final Mapeador mapeador;
    private final Clock reloj;

    public ServicioReservasImpl(RepositorioReservas repositorioReservas, RepositorioSalas repositorioSalas,
                                RepositorioMiembros repositorioMiembros,
                                RepositorioServiciosAdicionales repositorioServicios,
                                CalculadoraCostoReserva calculadora, Mapeador mapeador, Clock reloj) {
        this.repositorioReservas = repositorioReservas;
        this.repositorioSalas = repositorioSalas;
        this.repositorioMiembros = repositorioMiembros;
        this.repositorioServicios = repositorioServicios;
        this.calculadora = calculadora;
        this.mapeador = mapeador;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public RespuestaReserva crear(SolicitudReserva solicitud) {
        Miembro miembro = repositorioMiembros.buscarCompleto(solicitud.miembroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Miembro", solicitud.miembroId()));

        // RN-01 con concurrencia: se bloquea la fila de la sala antes de comprobar cruces.
        Sala sala = repositorioSalas.buscarParaActualizar(solicitud.salaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Sala", solicitud.salaId()));

        LocalDateTime inicio = solicitud.inicio();
        LocalDateTime fin = solicitud.fin();
        validarFormato(inicio, fin);
        validarMomento(inicio, fin, sala);
        validarSalaYAsistentes(sala, solicitud.asistentes());
        validarMiembro(miembro);
        validarLimiteDeReservas(miembro);
        validarSolapamiento(sala, inicio, fin);

        long minutos = Duration.between(inicio, fin).toMinutes();
        long minutosYaConsumidos = minutosConsumidosDelMes(miembro.getId(), inicio);
        CalculadoraCostoReserva.CostoSala costoSala =
                calculadora.calcularCostoSala(sala, miembro.getPlan(), minutos, minutosYaConsumidos);

        Reserva reserva = new Reserva(sala, miembro, inicio, fin, solicitud.asistentes(), EstadoReserva.PENDING);
        reserva.setCostoSala(costoSala.costo());
        agregarServiciosIniciales(reserva, solicitud.servicios());
        reserva.setCostoServicios(calculadora.calcularCostoServicios(reserva.getDetalles()));
        reserva.setCostoTotal(reserva.getCostoSala().add(reserva.getCostoServicios()));
        return mapeador.aRespuesta(repositorioReservas.save(reserva));
    }

    @Override
    public RespuestaReserva buscar(Long id) {
        return mapeador.aRespuesta(obtenerCompleta(id));
    }

    @Override
    public List<RespuestaReserva> listar(Long miembroId, Long salaId, LocalDate fecha, EstadoReserva estado) {
        int filtros = (miembroId == null ? 0 : 1) + (salaId == null ? 0 : 1)
                + (fecha == null ? 0 : 1) + (estado == null ? 0 : 1);
        if (filtros == 0) {
            throw new SolicitudInvalidaException(
                    "Indique un filtro: miembroId, salaId y fecha, o estado");
        }
        if (miembroId != null && (salaId != null || fecha != null || estado != null)) {
            throw new SolicitudInvalidaException(
                    "Use un solo filtro: miembroId, salaId y fecha, o estado");
        }
        if (estado != null && (salaId != null || fecha != null)) {
            throw new SolicitudInvalidaException(
                    "Use un solo filtro: miembroId, salaId y fecha, o estado");
        }
        if ((salaId == null) != (fecha == null)) {
            throw new SolicitudInvalidaException(
                    "Los filtros 'salaId' y 'fecha' deben usarse juntos");
        }

        List<Reserva> reservas;
        if (miembroId != null) {
            reservas = repositorioReservas.findByMiembroIdOrderByInicioDesc(miembroId);
        } else if (estado != null) {
            reservas = repositorioReservas.findByEstadoOrderByInicioAsc(estado);
        } else {
            LocalDateTime desde = fecha.atStartOfDay();
            LocalDateTime hasta = desde.plusDays(1);
            reservas = repositorioReservas.findBySalaIdAndInicioBetweenOrderByInicioAsc(salaId, desde, hasta);
        }
        return reservas.stream().map(mapeador::aRespuesta).toList();
    }

    @Override
    @Transactional
    public RespuestaReserva agregarServicio(Long reservaId, ItemServicio item) {
        Reserva reserva = obtenerCompleta(reservaId);
        if (reserva.getEstado() != EstadoReserva.PENDING) {
            throw new ReglaNegocioException("RN-10",
                    "Solo se pueden modificar los servicios de una reserva PENDING");
        }
        ServicioAdicional servicio = repositorioServicios.findById(item.servicioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio adicional", item.servicioId()));

        reserva.getDetalles().stream()
                .filter(detalle -> detalle.getServicio().getId().equals(servicio.getId()))
                .findFirst()
                .ifPresentOrElse(
                        detalle -> detalle.setCantidad(detalle.getCantidad() + item.cantidad()),
                        () -> reserva.agregarDetalle(new DetalleReserva(servicio, item.cantidad(),
                                servicio.getPrecioUnitario())));
        recalcularCostos(reserva);
        return mapeador.aRespuesta(repositorioReservas.save(reserva));
    }

    @Override
    @Transactional
    public RespuestaReserva quitarServicio(Long reservaId, Long servicioId) {
        Reserva reserva = obtenerCompleta(reservaId);
        if (reserva.getEstado() != EstadoReserva.PENDING) {
            throw new ReglaNegocioException("RN-10",
                    "Solo se pueden modificar los servicios de una reserva PENDING");
        }
        DetalleReserva detalle = reserva.getDetalles().stream()
                .filter(item -> item.getServicio().getId().equals(servicioId))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La reserva no tiene el servicio adicional con id " + servicioId));
        reserva.quitarDetalle(detalle);
        recalcularCostos(reserva);
        return mapeador.aRespuesta(repositorioReservas.save(reserva));
    }

    @Override
    @Transactional
    public RespuestaReserva confirmar(Long id) {
        Reserva reserva = obtenerCompleta(id);
        if (reserva.getEstado() != EstadoReserva.PENDING) {
            throw new ReglaNegocioException("RN-09", "Solo se puede confirmar una reserva PENDING");
        }
        reserva.setEstado(EstadoReserva.CONFIRMED);
        return mapeador.aRespuesta(repositorioReservas.save(reserva));
    }

    @Override
    @Transactional
    public RespuestaReserva cancelar(Long id) {
        Reserva reserva = obtenerCompleta(id);
        if (reserva.getEstado() != EstadoReserva.PENDING && reserva.getEstado() != EstadoReserva.CONFIRMED) {
            throw new ReglaNegocioException("RN-09",
                    "Solo se puede cancelar una reserva PENDING o CONFIRMED");
        }
        long minutosAnticipacion = Duration.between(LocalDateTime.now(reloj), reserva.getInicio()).toMinutes();
        if (minutosAnticipacion < 120) {
            throw new ReglaNegocioException("RN-08",
                    "No se puede cancelar con menos de 2 horas de anticipacion");
        }

        if (minutosAnticipacion >= 1440) {
            // 24 h o mas: sin cargo y las horas vuelven al cupo del mes.
            reserva.setCargoCancelacion(BigDecimal.ZERO.setScale(2));
            reserva.setCostoTotal(BigDecimal.ZERO.setScale(2));
            reserva.setMinutosConsumidos(0);
        } else {
            BigDecimal cargo = calculadora.calcularCargoCancelacion(reserva.getCostoSala());
            reserva.setCargoCancelacion(cargo);
            reserva.setCostoTotal(cargo);
        }
        reserva.setEstado(EstadoReserva.CANCELLED);
        return mapeador.aRespuesta(repositorioReservas.save(reserva));
    }

    @Override
    @Transactional
    public RespuestaReserva completar(Long id) {
        Reserva reserva = obtenerCompleta(id);
        if (reserva.getEstado() != EstadoReserva.CONFIRMED) {
            throw new ReglaNegocioException("RN-09", "Solo se puede completar una reserva CONFIRMED");
        }
        reserva.setEstado(EstadoReserva.COMPLETED);
        return mapeador.aRespuesta(repositorioReservas.save(reserva));
    }

    private void validarFormato(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            throw new SolicitudInvalidaException("Las fechas 'inicio' y 'fin' son obligatorias");
        }
        if (!inicio.isBefore(fin)) {
            throw new ReglaNegocioException("RN-02", "'inicio' debe ser anterior a 'fin'");
        }
        if (!inicio.toLocalDate().equals(fin.toLocalDate())) {
            throw new ReglaNegocioException("RN-02", "La reserva debe empezar y terminar el mismo dia");
        }
        long minutos = Duration.between(inicio, fin).toMinutes();
        if (minutos < 30 || minutos > 480 || minutos % 30 != 0) {
            throw new ReglaNegocioException("RN-02",
                    "La duracion debe ser de entre 30 minutos y 8 horas, en multiplos de 30 minutos");
        }
    }

    private void validarMomento(LocalDateTime inicio, LocalDateTime fin, Sala sala) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        if (inicio.isBefore(ahora.plusHours(1))) {
            throw new ReglaNegocioException("RN-03",
                    "La reserva debe ser futura y con al menos 1 hora de anticipacion");
        }
        LocalTime horaInicio = inicio.toLocalTime();
        LocalTime horaFin = fin.toLocalTime();
        if (horaInicio.isBefore(sala.getSede().getHoraApertura())
                || horaFin.isAfter(sala.getSede().getHoraCierre())) {
            throw new ReglaNegocioException("RN-03",
                    "La reserva debe estar dentro del horario de la sede");
        }
    }

    private void validarSalaYAsistentes(Sala sala, int asistentes) {
        if (!sala.isActiva()) {
            throw new ReglaNegocioException("RN-04", "La sala esta inactiva y no se puede reservar");
        }
        if (asistentes > sala.getCapacidad()) {
            throw new ReglaNegocioException("RN-04",
                    "Los asistentes superan la capacidad de la sala (" + sala.getCapacidad() + ")");
        }
    }

    private void validarMiembro(Miembro miembro) {
        if (miembro.getEstado() != EstadoMiembro.ACTIVE) {
            throw new ReglaNegocioException("RN-05", "Solo un miembro activo puede reservar");
        }
    }

    private void validarLimiteDeReservas(Miembro miembro) {
        long activas = repositorioReservas.countByMiembroIdAndEstadoInAndInicioAfter(
                miembro.getId(), ESTADOS_ACTIVOS, LocalDateTime.now(reloj));
        if (activas >= miembro.getPlan().getMaxReservasActivas()) {
            throw new ReglaNegocioException("RN-06",
                    "El miembro alcanzo el maximo de reservas activas de su plan ("
                            + miembro.getPlan().getMaxReservasActivas() + ")");
        }
    }

    private void validarSolapamiento(Sala sala, LocalDateTime inicio, LocalDateTime fin) {
        boolean seCruza = repositorioReservas.existsBySalaIdAndEstadoInAndInicioLessThanAndFinGreaterThan(
                sala.getId(), ESTADOS_ACTIVOS, fin, inicio);
        if (seCruza) {
            throw new ReglaNegocioException("RN-01", "La sala ya esta reservada en ese horario");
        }
    }

    private long minutosConsumidosDelMes(Long miembroId, LocalDateTime inicio) {
        LocalDateTime desde = inicio.withDayOfMonth(1).toLocalDate().atStartOfDay();
        return repositorioReservas.sumarMinutosConsumidos(miembroId, desde, desde.plusMonths(1));
    }

    private void agregarServiciosIniciales(Reserva reserva, List<ItemServicio> servicios) {
        if (servicios == null || servicios.isEmpty()) {
            return;
        }
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (ItemServicio item : servicios) {
            cantidades.merge(item.servicioId(), item.cantidad(), Integer::sum);
        }
        cantidades.forEach((servicioId, cantidad) -> {
            ServicioAdicional servicio = repositorioServicios.findById(servicioId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Servicio adicional", servicioId));
            reserva.agregarDetalle(new DetalleReserva(servicio, cantidad, servicio.getPrecioUnitario()));
        });
    }

    private void recalcularCostos(Reserva reserva) {
        reserva.setCostoServicios(calculadora.calcularCostoServicios(reserva.getDetalles()));
        reserva.setCostoTotal(reserva.getCostoSala().add(reserva.getCostoServicios()));
    }

    private Reserva obtenerCompleta(Long id) {
        return repositorioReservas.buscarCompleta(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva", id));
    }
}
