package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Plan de membresia devuelto por la API")
public record RespuestaPlan(
        @Schema(example = "2") Long id,
        @Schema(example = "Profesional") String nombre,
        @Schema(example = "40") int horasIncluidasMes,
        @Schema(example = "10") int descuentoExcedente,
        @Schema(example = "5") int maxReservasActivas) {
}
