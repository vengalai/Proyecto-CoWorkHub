package com.coworkhub.presentacion.dto;

import com.coworkhub.negocio.model.EstadoMiembro;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Miembro devuelto por la API (sin datos de contrasena)")
public record RespuestaMiembro(
        @Schema(example = "1") Long id,
        @Schema(example = "30111222") String documento,
        @Schema(example = "Ana Gomez") String nombre,
        @Schema(example = "ana.gomez@example.com") String email,
        @Schema(example = "ACTIVE") EstadoMiembro estado,
        @Schema(example = "2") Long planId,
        @Schema(example = "Profesional") String plan,
        @Schema(example = "ana") String nombreUsuario) {
}
