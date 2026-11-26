package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Servicio adicional pedido con una cantidad")
public record ItemServicio(
        @Schema(description = "Id del servicio adicional", example = "1")
        @NotNull(message = "es obligatorio")
        Long servicioId,

        @Schema(description = "Cantidad", example = "1")
        @Min(value = 1, message = "debe ser mayor que cero")
        int cantidad) {
}
