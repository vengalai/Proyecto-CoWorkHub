package com.coworkhub.presentacion.dto;

import com.coworkhub.negocio.model.TipoSala;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Sala devuelta por la API")
public record RespuestaSala(
        @Schema(example = "1") Long id,
        @Schema(example = "Sala Aurora") String nombre,
        @Schema(example = "MEETING_ROOM") TipoSala tipo,
        @Schema(example = "8") int capacidad,
        @Schema(example = "35.00") BigDecimal tarifaPorHora,
        @Schema(example = "true") boolean activa,
        @Schema(example = "1") Long sedeId,
        @Schema(example = "Sede Centro") String sede,
        List<RespuestaEquipamiento> equipamientos) {
}
