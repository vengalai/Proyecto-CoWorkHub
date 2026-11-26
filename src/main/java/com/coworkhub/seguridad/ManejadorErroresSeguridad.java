package com.coworkhub.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

/**
 * Respuestas JSON uniformes cuando la peticion no trae token (401)
 * o el usuario no tiene permiso (403). Evita la pagina HTML por defecto.
 */
@Component
public class ManejadorErroresSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest peticion, HttpServletResponse respuesta,
                         AuthenticationException excepcion) throws IOException {
        escribir(respuesta, HttpServletResponse.SC_UNAUTHORIZED,
                "Debe iniciar sesion para acceder a este recurso", "NO_AUTENTICADO");
    }

    @Override
    public void handle(HttpServletRequest peticion, HttpServletResponse respuesta,
                       AccessDeniedException excepcion) throws IOException {
        escribir(respuesta, HttpServletResponse.SC_FORBIDDEN,
                "No tiene permiso para realizar esta operacion", "ACCESO_DENEGADO");
    }

    private void escribir(HttpServletResponse respuesta, int estado, String mensaje, String codigo)
            throws IOException {
        respuesta.setStatus(estado);
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String cuerpo = "{\"error\":\"" + mensaje + "\",\"codigo\":\"" + codigo
                + "\",\"timestamp\":\"" + OffsetDateTime.now() + "\"}";
        respuesta.getWriter().write(cuerpo);
    }
}
