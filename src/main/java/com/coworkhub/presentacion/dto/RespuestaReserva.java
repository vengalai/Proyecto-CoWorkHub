package com.coworkhub.presentacion.dto;

import com.coworkhub.negocio.model.EstadoReserva;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Reserva devuelta por la API")
public record RespuestaReserva(
        @Schema(example = "1") Long id,
        @Schema(example = "1") Long miembroId,
        @Schema(example = "Ana Gomez") String miembro,
        @Schema(example = "1") Long salaId,
        @Schema(example = "Sala Aurora") String sala,
        @Schema(example = "Sede Centro") String sede,
        @Schema(example = "2030-06-04T16:00:00") LocalDateTime inicio,
        @Schema(example = "2030-06-04T18:00:00") LocalDateTime fin,
        @Schema(example = "4") int asistentes,
        @Schema(example = "PENDING") EstadoReserva estado,
        @Schema(example = "70.00") BigDecimal costoSala,
        @Schema(example = "80.00") BigDecimal costoServicios,
        @Schema(example = "150.00") BigDecimal costoTotal,
        @Schema(example = "0.00") BigDecimal cargoCancelacion,
        List<RespuestaDetalleReserva> detalles) {
}
