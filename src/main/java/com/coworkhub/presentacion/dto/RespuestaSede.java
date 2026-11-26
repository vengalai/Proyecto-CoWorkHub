package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

@Schema(description = "Sede devuelta por la API")
public record RespuestaSede(
        @Schema(example = "1") Long id,
        @Schema(example = "Sede Centro") String nombre,
        @Schema(example = "Buenos Aires") String ciudad,
        @Schema(example = "Av. Corrientes 1234") String direccion,
        @Schema(example = "08:00") LocalTime horaApertura,
        @Schema(example = "20:00") LocalTime horaCierre) {
}
