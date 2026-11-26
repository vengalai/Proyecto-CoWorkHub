package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

@Schema(description = "Datos para crear o actualizar una sede")
public record SolicitudSede(
        @Schema(description = "Nombre de la sede", example = "Sede Centro")
        @NotBlank(message = "es obligatorio")
        @Size(max = 80, message = "no puede superar los 80 caracteres")
        String nombre,

        @Schema(description = "Ciudad", example = "Buenos Aires")
        @NotBlank(message = "es obligatorio")
        @Size(max = 60, message = "no puede superar los 60 caracteres")
        String ciudad,

        @Schema(description = "Direccion", example = "Av. Corrientes 1234")
        @NotBlank(message = "es obligatorio")
        @Size(max = 120, message = "no puede superar los 120 caracteres")
        String direccion,

        @Schema(description = "Hora de apertura", example = "08:00")
        @NotNull(message = "es obligatoria")
        LocalTime horaApertura,

        @Schema(description = "Hora de cierre", example = "20:00")
        @NotNull(message = "es obligatoria")
        LocalTime horaCierre) {
}
