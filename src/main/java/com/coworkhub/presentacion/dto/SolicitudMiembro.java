package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar un miembro y su cuenta de usuario")
public record SolicitudMiembro(
        @Schema(description = "Numero de documento", example = "30111222")
        @NotBlank(message = "es obligatorio")
        @Size(max = 20, message = "no puede superar los 20 caracteres")
        String documento,

        @Schema(description = "Nombre completo", example = "Ana Gomez")
        @NotBlank(message = "es obligatorio")
        @Size(max = 100, message = "no puede superar los 100 caracteres")
        String nombre,

        @Schema(description = "Email", example = "ana.gomez@example.com")
        @NotBlank(message = "es obligatorio")
        @Email(message = "debe tener un formato valido")
        String email,

        @Schema(description = "Id del plan", example = "2")
        @NotNull(message = "es obligatorio")
        Long planId,

        @Schema(description = "Nombre de usuario", example = "ana")
        @NotBlank(message = "es obligatorio")
        @Size(min = 3, max = 30, message = "debe tener entre 3 y 30 caracteres")
        String nombreUsuario,

        @Schema(description = "Contrasena", example = "coworkhub123")
        @NotBlank(message = "es obligatoria")
        @Size(min = 6, message = "debe tener al menos 6 caracteres")
        String contrasena) {
}
