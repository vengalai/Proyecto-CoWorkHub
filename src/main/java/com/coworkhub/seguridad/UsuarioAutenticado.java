package com.coworkhub.seguridad;

import com.coworkhub.negocio.model.Rol;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Usuario que Spring Security guarda en el contexto durante una peticion.
 * Incluye el id del miembro cuando la cuenta pertenece a un miembro.
 */
public class UsuarioAutenticado implements UserDetails {

    private final Long id;
    private final String nombreUsuario;
    private final String contrasena;
    private final Rol rol;
    private final Long miembroId;

    public UsuarioAutenticado(Long id, String nombreUsuario, String contrasena, Rol rol, Long miembroId) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.rol = rol;
        this.miembroId = miembroId;
    }

    public Long getId() {
        return id;
    }

    public Rol getRol() {
        return rol;
    }

    public Long getMiembroId() {
        return miembroId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return contrasena;
    }

    @Override
    public String getUsername() {
        return nombreUsuario;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
