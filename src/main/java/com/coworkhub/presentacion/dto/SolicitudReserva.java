package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Datos para crear una reserva")
public record SolicitudReserva(
        @Schema(description = "Id del miembro", example = "1")
        @NotNull(message = "es obligatorio")
        Long miembroId,

        @Schema(description = "Id de la sala", example = "1")
        @NotNull(message = "es obligatorio")
        Long salaId,

        @Schema(description = "Inicio de la reserva", example = "2030-06-04T16:00:00")
        @NotNull(message = "es obligatorio")
        LocalDateTime inicio,

        @Schema(description = "Fin de la reserva", example = "2030-06-04T18:00:00")
        @NotNull(message = "es obligatorio")
        LocalDateTime fin,

        @Schema(description = "Cantidad de asistentes", example = "4")
        @Min(value = 1, message = "debe ser mayor que cero")
        int asistentes,

        @Schema(description = "Servicios adicionales opcionales")
        List<@Valid ItemServicio> servicios) {
}
