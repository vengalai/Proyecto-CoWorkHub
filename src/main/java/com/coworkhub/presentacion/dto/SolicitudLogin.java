package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales de inicio de sesion")
public record SolicitudLogin(
        @Schema(description = "Nombre de usuario", example = "admin")
        @NotBlank(message = "es obligatorio")
        String nombreUsuario,

        @Schema(description = "Contrasena", example = "coworkhub123")
        @NotBlank(message = "es obligatoria")
        String contrasena) {
}
