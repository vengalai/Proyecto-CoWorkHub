package com.coworkhub.presentacion.dto;

import com.coworkhub.negocio.model.TipoSala;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Datos para crear o actualizar una sala")
public record SolicitudSala(
        @Schema(description = "Nombre de la sala", example = "Sala Aurora")
        @NotBlank(message = "es obligatorio")
        @Size(max = 80, message = "no puede superar los 80 caracteres")
        String nombre,

        @Schema(description = "Tipo de sala", example = "MEETING_ROOM")
        @NotNull(message = "es obligatorio")
        TipoSala tipo,

        @Schema(description = "Capacidad de personas", example = "8")
        @Min(value = 1, message = "debe ser mayor que cero")
        int capacidad,

        @Schema(description = "Tarifa por hora", example = "35.00")
        @NotNull(message = "es obligatoria")
        @DecimalMin(value = "0.01", message = "debe ser mayor que cero")
        BigDecimal tarifaPorHora,

        @Schema(description = "Indica si la sala se puede reservar", example = "true")
        Boolean activa,

        @Schema(description = "Id de la sede", example = "1")
        @NotNull(message = "es obligatorio")
        Long sedeId,

        @Schema(description = "Ids del equipamiento de la sala", example = "[1, 2]")
        List<Long> equipamientoIds) {
}
