package com.coworkhub.seguridad;

import com.coworkhub.negocio.excepcion.NoAutenticadoException;
import com.coworkhub.presentacion.dto.RespuestaLogin;
import com.coworkhub.presentacion.dto.RespuestaPerfil;
import com.coworkhub.presentacion.dto.SolicitudLogin;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ServicioAutenticacionImpl implements ServicioAutenticacion {

    private final AuthenticationManager gestorAutenticacion;
    private final JwtServicio jwtServicio;

    public ServicioAutenticacionImpl(AuthenticationManager gestorAutenticacion, JwtServicio jwtServicio) {
        this.gestorAutenticacion = gestorAutenticacion;
        this.jwtServicio = jwtServicio;
    }

    @Override
    public RespuestaLogin iniciarSesion(SolicitudLogin solicitud) {
        Authentication autenticacion = gestorAutenticacion.authenticate(
                new UsernamePasswordAuthenticationToken(solicitud.nombreUsuario(), solicitud.contrasena()));
        UsuarioAutenticado usuario = (UsuarioAutenticado) autenticacion.getPrincipal();
        return new RespuestaLogin(jwtServicio.generarToken(usuario), usuario.getRol(),
                jwtServicio.getExpiracionMinutos());
    }

    @Override
    public RespuestaPerfil perfil() {
        UsuarioAutenticado usuario = usuarioActual();
        return new RespuestaPerfil(usuario.getUsername(), usuario.getRol(), usuario.getMiembroId());
    }

    private UsuarioAutenticado usuarioActual() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion != null && autenticacion.getPrincipal() instanceof UsuarioAutenticado usuario) {
            return usuario;
        }
        throw new NoAutenticadoException("No hay una sesion iniciada");
    }
}
