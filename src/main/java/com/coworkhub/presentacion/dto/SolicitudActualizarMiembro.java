package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos que se pueden actualizar de un miembro")
public record SolicitudActualizarMiembro(
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
        Long planId) {
}
