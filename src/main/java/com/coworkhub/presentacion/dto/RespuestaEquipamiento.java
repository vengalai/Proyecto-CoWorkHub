package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Equipamiento devuelto por la API")
public record RespuestaEquipamiento(
        @Schema(example = "1") Long id,
        @Schema(example = "Proyector") String nombre) {
}
