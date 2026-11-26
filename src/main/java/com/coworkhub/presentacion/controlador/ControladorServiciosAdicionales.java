package com.coworkhub.presentacion.controlador;

import com.coworkhub.negocio.servicio.ServicioServiciosAdicionales;
import com.coworkhub.presentacion.dto.RespuestaServicioAdicional;
import com.coworkhub.presentacion.dto.SolicitudServicioAdicional;
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
@RequestMapping("/api/servicios-adicionales")
@Tag(name = "Servicios adicionales", description = "Servicios que se cobran por cantidad en una reserva")
public class ControladorServiciosAdicionales {

    private final ServicioServiciosAdicionales servicioServicios;

    public ControladorServiciosAdicionales(ServicioServiciosAdicionales servicioServicios) {
        this.servicioServicios = servicioServicios;
    }

    @GetMapping
    @Operation(summary = "Lista los servicios adicionales")
    public List<RespuestaServicioAdicional> listar() {
        return servicioServicios.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta un servicio adicional")
    public RespuestaServicioAdicional buscar(@PathVariable Long id) {
        return servicioServicios.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crea un servicio adicional")
    public RespuestaServicioAdicional crear(@Valid @RequestBody SolicitudServicioAdicional solicitud) {
        return servicioServicios.crear(solicitud);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualiza un servicio adicional, incluido su precio")
    public RespuestaServicioAdicional actualizar(@PathVariable Long id,
                                                 @Valid @RequestBody SolicitudServicioAdicional solicitud) {
        return servicioServicios.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Elimina un servicio adicional")
    public void eliminar(@PathVariable Long id) {
        servicioServicios.eliminar(id);
    }
}
