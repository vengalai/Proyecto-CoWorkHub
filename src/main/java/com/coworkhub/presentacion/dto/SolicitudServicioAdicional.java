package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Datos para crear o actualizar un servicio adicional")
public record SolicitudServicioAdicional(
        @Schema(description = "Nombre del servicio", example = "Catering basico")
        @NotBlank(message = "es obligatorio")
        @Size(max = 60, message = "no puede superar los 60 caracteres")
        String nombre,

        @Schema(description = "Precio por unidad", example = "50.00")
        @NotNull(message = "es obligatorio")
        @DecimalMin(value = "0.01", message = "debe ser mayor que cero")
        BigDecimal precioUnitario) {
}
