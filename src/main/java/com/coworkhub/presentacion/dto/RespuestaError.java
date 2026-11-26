package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Cuerpo unico de error de la API")
public record RespuestaError(
        @Schema(example = "La sala ya esta reservada en ese horario") String error,
        @Schema(example = "RN-01") String codigo,
        @Schema(example = "2030-06-03T13:00:00.123Z") OffsetDateTime timestamp) {
}
