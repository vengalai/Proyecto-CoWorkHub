package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear o actualizar un equipamiento")
public record SolicitudEquipamiento(
        @Schema(description = "Nombre del equipamiento", example = "Proyector")
        @NotBlank(message = "es obligatorio")
        @Size(max = 60, message = "no puede superar los 60 caracteres")
        String nombre) {
}
