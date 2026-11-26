package com.coworkhub.presentacion.controlador;

import com.coworkhub.negocio.model.EstadoReserva;
import com.coworkhub.negocio.servicio.ServicioReservas;
import com.coworkhub.presentacion.dto.ItemServicio;
import com.coworkhub.presentacion.dto.RespuestaReserva;
import com.coworkhub.presentacion.dto.SolicitudReserva;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Reservas de salas y sus servicios")
public class ControladorReservas {

    private final ServicioReservas servicioReservas;

    public ControladorReservas(ServicioReservas servicioReservas) {
        this.servicioReservas = servicioReservas;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@autorizacionServicio.puedeCrearReserva(#solicitud.miembroId())")
    @Operation(summary = "Crea una reserva con sus servicios")
    @ApiResponse(responseCode = "409", description = "Se violo una regla RN-01 a RN-07")
    public RespuestaReserva crear(@Valid @RequestBody SolicitudReserva solicitud) {
        return servicioReservas.crear(solicitud);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@autorizacionServicio.puedeVerReserva(#id)")
    @Operation(summary = "Consulta una reserva")
    public RespuestaReserva buscar(@PathVariable Long id) {
        return servicioReservas.buscar(id);
    }

    @GetMapping
    @PreAuthorize("@autorizacionServicio.puedeListarReservas(#miembroId, #salaId, #estado)")
    @Operation(summary = "Lista reservas con un unico filtro")
    public List<RespuestaReserva> listar(
            @RequestParam(required = false) Long miembroId,
            @RequestParam(required = false) Long salaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) EstadoReserva estado) {
        return servicioReservas.listar(miembroId, salaId, fecha, estado);
    }

    @PostMapping("/{id}/servicios")
    @PreAuthorize("@autorizacionServicio.puedeVerReserva(#id)")
    @Operation(summary = "Agrega un servicio adicional a una reserva PENDING")
    public RespuestaReserva agregarServicio(@PathVariable Long id,
                                            @Valid @RequestBody ItemServicio item) {
        return servicioReservas.agregarServicio(id, item);
    }

    @DeleteMapping("/{id}/servicios/{servicioId}")
    @PreAuthorize("@autorizacionServicio.puedeVerReserva(#id)")
    @Operation(summary = "Quita un servicio adicional de una reserva PENDING")
    public RespuestaReserva quitarServicio(@PathVariable Long id, @PathVariable Long servicioId) {
        return servicioReservas.quitarServicio(id, servicioId);
    }

    @PostMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Operation(summary = "Confirma una reserva PENDING")
    public RespuestaReserva confirmar(@PathVariable Long id) {
        return servicioReservas.confirmar(id);
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("@autorizacionServicio.puedeVerReserva(#id)")
    @Operation(summary = "Cancela una reserva y calcula el cargo")
    public RespuestaReserva cancelar(@PathVariable Long id) {
        return servicioReservas.cancelar(id);
    }

    @PostMapping("/{id}/completar")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Operation(summary = "Completa una reserva CONFIRMED")
    public RespuestaReserva completar(@PathVariable Long id) {
        return servicioReservas.completar(id);
    }
}
