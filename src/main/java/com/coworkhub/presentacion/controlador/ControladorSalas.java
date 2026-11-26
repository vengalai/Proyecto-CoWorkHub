package com.coworkhub.presentacion.controlador;

import com.coworkhub.negocio.servicio.ServicioSalas;
import com.coworkhub.presentacion.dto.RespuestaSala;
import com.coworkhub.presentacion.dto.SolicitudSala;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/salas")
@Tag(name = "Salas", description = "Catalogo de salas y su equipamiento")
public class ControladorSalas {

    private final ServicioSalas servicioSalas;

    public ControladorSalas(ServicioSalas servicioSalas) {
        this.servicioSalas = servicioSalas;
    }

    @GetMapping
    @Operation(summary = "Lista las salas, opcionalmente filtradas por sede")
    public List<RespuestaSala> listar(@RequestParam(required = false) Long sedeId) {
        return servicioSalas.listar(sedeId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta una sala")
    public RespuestaSala buscar(@PathVariable Long id) {
        return servicioSalas.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crea una sala")
    @ApiResponse(responseCode = "409", description = "Nombre repetido en la sede (RN-11)")
    public RespuestaSala crear(@Valid @RequestBody SolicitudSala solicitud) {
        return servicioSalas.crear(solicitud);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualiza una sala y su equipamiento")
    public RespuestaSala actualizar(@PathVariable Long id, @Valid @RequestBody SolicitudSala solicitud) {
        return servicioSalas.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Elimina una sala")
    @ApiResponse(responseCode = "409", description = "La sala tiene reservas (RN-12)")
    public void eliminar(@PathVariable Long id) {
        servicioSalas.eliminar(id);
    }
}
