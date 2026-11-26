package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Servicio adicional devuelto por la API")
public record RespuestaServicioAdicional(
        @Schema(example = "1") Long id,
        @Schema(example = "Catering basico") String nombre,
        @Schema(example = "50.00") BigDecimal precioUnitario) {
}
