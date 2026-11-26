package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Detalle de un servicio dentro de una reserva")
public record RespuestaDetalleReserva(
        @Schema(example = "1") Long servicioId,
        @Schema(example = "Catering basico") String servicio,
        @Schema(example = "2") int cantidad,
        @Schema(example = "50.00") BigDecimal precioUnitarioAplicado,
        @Schema(example = "100.00") BigDecimal subtotal) {
}
