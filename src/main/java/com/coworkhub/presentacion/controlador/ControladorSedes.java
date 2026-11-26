package com.coworkhub.presentacion.controlador;

import com.coworkhub.negocio.servicio.ServicioSedes;
import com.coworkhub.presentacion.dto.RespuestaSede;
import com.coworkhub.presentacion.dto.SolicitudSede;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sedes")
@Tag(name = "Sedes", description = "Catalogo de sedes del coworking")
public class ControladorSedes {

    private final ServicioSedes servicioSedes;

    public ControladorSedes(ServicioSedes servicioSedes) {
        this.servicioSedes = servicioSedes;
    }

    @GetMapping
    @Operation(summary = "Lista las sedes")
    public List<RespuestaSede> listar() {
        return servicioSedes.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta una sede")
    @ApiResponse(responseCode = "404", description = "La sede no existe")
    public RespuestaSede buscar(@PathVariable Long id) {
        return servicioSedes.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crea una sede")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    public RespuestaSede crear(@Valid @RequestBody SolicitudSede solicitud) {
        return servicioSedes.crear(solicitud);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualiza una sede")
    public RespuestaSede actualizar(@PathVariable Long id, @Valid @RequestBody SolicitudSede solicitud) {
        return servicioSedes.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Elimina una sede")
    @ApiResponse(responseCode = "409", description = "La sede tiene salas (RN-12)")
    public void eliminar(@PathVariable Long id) {
        servicioSedes.eliminar(id);
    }
}
