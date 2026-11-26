package com.coworkhub.presentacion.controlador;

import com.coworkhub.negocio.servicio.ServicioEquipamientos;
import com.coworkhub.presentacion.dto.RespuestaEquipamiento;
import com.coworkhub.presentacion.dto.SolicitudEquipamiento;
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
@RequestMapping("/api/equipamientos")
@Tag(name = "Equipamientos", description = "Catalogo de equipamiento compartido")
public class ControladorEquipamientos {

    private final ServicioEquipamientos servicioEquipamientos;

    public ControladorEquipamientos(ServicioEquipamientos servicioEquipamientos) {
        this.servicioEquipamientos = servicioEquipamientos;
    }

    @GetMapping
    @Operation(summary = "Lista el equipamiento")
    public List<RespuestaEquipamiento> listar() {
        return servicioEquipamientos.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta un equipamiento")
    public RespuestaEquipamiento buscar(@PathVariable Long id) {
        return servicioEquipamientos.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crea un equipamiento")
    public RespuestaEquipamiento crear(@Valid @RequestBody SolicitudEquipamiento solicitud) {
        return servicioEquipamientos.crear(solicitud);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualiza un equipamiento")
    public RespuestaEquipamiento actualizar(@PathVariable Long id,
                                            @Valid @RequestBody SolicitudEquipamiento solicitud) {
        return servicioEquipamientos.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Elimina un equipamiento")
    public void eliminar(@PathVariable Long id) {
        servicioEquipamientos.eliminar(id);
    }
}
