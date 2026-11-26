package com.coworkhub.presentacion.dto;

import com.coworkhub.negocio.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Perfil del usuario autenticado")
public record RespuestaPerfil(
        @Schema(example = "ana") String nombreUsuario,
        @Schema(example = "MEMBER") Rol rol,
        @Schema(example = "1") Long miembroId) {
}
