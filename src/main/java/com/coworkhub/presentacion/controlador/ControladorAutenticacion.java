package com.coworkhub.presentacion.controlador;

import com.coworkhub.presentacion.dto.RespuestaLogin;
import com.coworkhub.presentacion.dto.RespuestaPerfil;
import com.coworkhub.presentacion.dto.SolicitudLogin;
import com.coworkhub.seguridad.ServicioAutenticacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticacion", description = "Inicio de sesion y perfil del usuario")
public class ControladorAutenticacion {

    private final ServicioAutenticacion servicioAutenticacion;

    public ControladorAutenticacion(ServicioAutenticacion servicioAutenticacion) {
        this.servicioAutenticacion = servicioAutenticacion;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Inicia sesion y devuelve un token JWT")
    @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    public RespuestaLogin iniciarSesion(@Valid @RequestBody SolicitudLogin solicitud) {
        return servicioAutenticacion.iniciarSesion(solicitud);
    }

    @GetMapping("/perfil")
    @Operation(summary = "Devuelve el perfil del usuario autenticado")
    public RespuestaPerfil perfil() {
        return servicioAutenticacion.perfil();
    }
}
