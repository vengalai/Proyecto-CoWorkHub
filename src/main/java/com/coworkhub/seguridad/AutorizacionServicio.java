package com.coworkhub.seguridad;

import com.coworkhub.negocio.excepcion.AccesoDenegadoException;
import com.coworkhub.negocio.model.EstadoReserva;
import com.coworkhub.negocio.model.Rol;
import com.coworkhub.persistencia.repositorio.RepositorioReservas;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Reglas de acceso que dependen del dueno del recurso (RN-13).
 * Se usan desde @PreAuthorize con el nombre "autorizacionServicio".
 */
@Component("autorizacionServicio")
public class AutorizacionServicio {

    private final RepositorioReservas repositorioReservas;

    public AutorizacionServicio(RepositorioReservas repositorioReservas) {
        this.repositorioReservas = repositorioReservas;
    }

    public boolean esPersonal() {
        Rol rol = usuarioActual().getRol();
        return rol == Rol.ADMIN || rol == Rol.RECEPTIONIST;
    }

    public boolean puedeVerMiembro(Long miembroId) {
        return esPersonal() || esMiembroPropio(miembroId);
    }

    public boolean puedeVerConsumo(Long miembroId) {
        return esPersonal() || esMiembroPropio(miembroId);
    }

    public boolean puedeCrearReserva(Long miembroId) {
        return esPersonal() || esMiembroPropio(miembroId);
    }

    public boolean puedeVerReserva(Long reservaId) {
        if (esPersonal()) {
            return true;
        }
        Long miembroId = usuarioActual().getMiembroId();
        return miembroId != null && repositorioReservas.existsByIdAndMiembroId(reservaId, miembroId);
    }

    public boolean puedeListarReservas(Long miembroId, Long salaId, EstadoReserva estado) {
        if (esPersonal()) {
            return true;
        }
        return miembroId != null && esMiembroPropio(miembroId);
    }

    private boolean esMiembroPropio(Long miembroId) {
        return miembroId != null && Objects.equals(miembroId, usuarioActual().getMiembroId());
    }

    private UsuarioAutenticado usuarioActual() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion != null && autenticacion.getPrincipal() instanceof UsuarioAutenticado usuario) {
            return usuario;
        }
        throw new AccesoDenegadoException("No hay una sesion autenticada");
    }
}
