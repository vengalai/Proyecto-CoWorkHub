package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear o actualizar un plan de membresia")
public record SolicitudPlan(
        @Schema(description = "Nombre del plan", example = "Profesional")
        @NotBlank(message = "es obligatorio")
        @Size(max = 60, message = "no puede superar los 60 caracteres")
        String nombre,

        @Schema(description = "Horas incluidas por mes", example = "40")
        @Min(value = 0, message = "no puede ser negativo")
        int horasIncluidasMes,

        @Schema(description = "Descuento sobre horas excedentes (%)", example = "10")
        @Min(value = 0, message = "debe estar entre 0 y 100")
        @Max(value = 100, message = "debe estar entre 0 y 100")
        int descuentoExcedente,

        @Schema(description = "Maximo de reservas activas simultaneas", example = "5")
        @Min(value = 1, message = "debe ser mayor que cero")
        int maxReservasActivas) {
}
