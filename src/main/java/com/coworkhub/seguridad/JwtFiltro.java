package com.coworkhub.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lee el encabezado Authorization: Bearer &lt;token&gt; y, si el token es valido,
 * deja al usuario autenticado en el contexto de Spring Security.
 */
@Component
public class JwtFiltro extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtServicio jwtServicio;
    private final UsuarioDetallesService usuarioDetallesService;

    public JwtFiltro(JwtServicio jwtServicio, UsuarioDetallesService usuarioDetallesService) {
        this.jwtServicio = jwtServicio;
        this.usuarioDetallesService = usuarioDetallesService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta,
                                    FilterChain cadena) throws ServletException, IOException {
        String cabecera = peticion.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith(PREFIJO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = cabecera.substring(PREFIJO.length());
            if (jwtServicio.esValido(token)) {
                autenticar(token, peticion);
            }
        }
        cadena.doFilter(peticion, respuesta);
    }

    private void autenticar(String token, HttpServletRequest peticion) {
        try {
            String nombreUsuario = jwtServicio.extraerNombreUsuario(token);
            UserDetails usuario = usuarioDetallesService.loadUserByUsername(nombreUsuario);
            UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(
                    usuario, null, usuario.getAuthorities());
            autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(peticion));
            SecurityContextHolder.getContext().setAuthentication(autenticacion);
        } catch (RuntimeException excepcion) {
            // Token valido pero el usuario ya no existe: se deja la peticion sin autenticar.
            SecurityContextHolder.clearContext();
        }
    }
}
