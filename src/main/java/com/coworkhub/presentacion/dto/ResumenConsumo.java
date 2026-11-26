package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Resumen de consumo mensual de un miembro")
public record ResumenConsumo(
        @Schema(example = "1") Long miembroId,
        @Schema(example = "Ana Gomez") String miembro,
        @Schema(example = "Profesional") String plan,
        @Schema(example = "2030-06") String mes,
        @Schema(example = "40.00") BigDecimal horasIncluidas,
        @Schema(example = "3.00") BigDecimal horasUsadas,
        @Schema(example = "3.00") BigDecimal horasIncluidasUsadas,
        @Schema(example = "37.00") BigDecimal horasRestantes,
        @Schema(example = "0.00") BigDecimal horasExcedentes) {
}
