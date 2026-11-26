package com.coworkhub.presentacion.controlador;

import com.coworkhub.negocio.model.EstadoMiembro;
import com.coworkhub.negocio.servicio.ServicioMiembros;
import com.coworkhub.presentacion.dto.Pagina;
import com.coworkhub.presentacion.dto.RespuestaMiembro;
import com.coworkhub.presentacion.dto.ResumenConsumo;
import com.coworkhub.presentacion.dto.SolicitudActualizarMiembro;
import com.coworkhub.presentacion.dto.SolicitudMiembro;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/miembros")
@Tag(name = "Miembros", description = "Gestion de miembros y su consumo")
public class ControladorMiembros {

    private final ServicioMiembros servicioMiembros;

    public ControladorMiembros(ServicioMiembros servicioMiembros) {
        this.servicioMiembros = servicioMiembros;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Operation(summary = "Lista miembros de forma paginada y filtrada")
    public Pagina<RespuestaMiembro> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) EstadoMiembro estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombre,asc") String sort) {
        return servicioMiembros.listar(nombre, estado, page, size, sort);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@autorizacionServicio.puedeVerMiembro(#id)")
    @Operation(summary = "Consulta un miembro (personal o el propio miembro)")
    public RespuestaMiembro buscar(@PathVariable Long id) {
        return servicioMiembros.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Operation(summary = "Registra un miembro y su cuenta de usuario")
    public RespuestaMiembro crear(@Valid @RequestBody SolicitudMiembro solicitud) {
        return servicioMiembros.crear(solicitud);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Operation(summary = "Actualiza nombre, email y plan de un miembro")
    public RespuestaMiembro actualizar(@PathVariable Long id,
                                       @Valid @RequestBody SolicitudActualizarMiembro solicitud) {
        return servicioMiembros.actualizar(id, solicitud);
    }

    @PatchMapping("/{id}/suspender")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Operation(summary = "Suspende un miembro")
    public RespuestaMiembro suspender(@PathVariable Long id) {
        return servicioMiembros.suspender(id);
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Operation(summary = "Reactiva un miembro")
    public RespuestaMiembro activar(@PathVariable Long id) {
        return servicioMiembros.activar(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Operation(summary = "Elimina un miembro y su cuenta")
    public void eliminar(@PathVariable Long id) {
        servicioMiembros.eliminar(id);
    }

    @GetMapping("/{id}/consumo")
    @PreAuthorize("@autorizacionServicio.puedeVerConsumo(#id)")
    @Operation(summary = "Resumen de consumo mensual de un miembro")
    public ResumenConsumo consumo(@PathVariable Long id,
                                  @RequestParam(required = false) String mes) {
        return servicioMiembros.consumo(id, mes);
    }
}
