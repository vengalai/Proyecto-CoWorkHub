package com.coworkhub.presentacion.dto;

import com.coworkhub.negocio.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta del inicio de sesion")
public record RespuestaLogin(
        String token,
        @Schema(example = "ADMIN") Rol rol,
        @Schema(example = "60") long expiraEnMinutos) {
}
