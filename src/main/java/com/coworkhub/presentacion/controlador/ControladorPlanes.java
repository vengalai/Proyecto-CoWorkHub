package com.coworkhub.presentacion.controlador;

import com.coworkhub.negocio.servicio.ServicioPlanes;
import com.coworkhub.presentacion.dto.RespuestaPlan;
import com.coworkhub.presentacion.dto.SolicitudPlan;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/planes")
@Tag(name = "Planes", description = "Planes de membresia")
public class ControladorPlanes {

    private final ServicioPlanes servicioPlanes;

    public ControladorPlanes(ServicioPlanes servicioPlanes) {
        this.servicioPlanes = servicioPlanes;
    }

    @GetMapping
    @Operation(summary = "Lista los planes")
    public List<RespuestaPlan> listar() {
        return servicioPlanes.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta un plan")
    public RespuestaPlan buscar(@PathVariable Long id) {
        return servicioPlanes.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crea un plan")
    public RespuestaPlan crear(@Valid @RequestBody SolicitudPlan solicitud) {
        return servicioPlanes.crear(solicitud);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualiza un plan")
    public RespuestaPlan actualizar(@PathVariable Long id, @Valid @RequestBody SolicitudPlan solicitud) {
        return servicioPlanes.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Elimina un plan")
    public void eliminar(@PathVariable Long id) {
        servicioPlanes.eliminar(id);
    }
}
