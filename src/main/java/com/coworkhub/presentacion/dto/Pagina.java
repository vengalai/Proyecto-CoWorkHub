package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Pagina de resultados")
public record Pagina<T>(
        List<T> contenido,
        @Schema(example = "0") int pagina,
        @Schema(example = "10") int tamano,
        @Schema(example = "5") long totalElementos,
        @Schema(example = "1") int totalPaginas) {
}
