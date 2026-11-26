package com.coworkhub.seguridad;

import com.coworkhub.negocio.model.CuentaUsuario;
import com.coworkhub.persistencia.repositorio.RepositorioCuentasUsuario;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioDetallesService implements UserDetailsService {

    private final RepositorioCuentasUsuario repositorioCuentas;

    public UsuarioDetallesService(RepositorioCuentasUsuario repositorioCuentas) {
        this.repositorioCuentas = repositorioCuentas;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String nombreUsuario) {
        CuentaUsuario cuenta = repositorioCuentas.findByNombreUsuarioIgnoreCase(nombreUsuario)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No existe el usuario '" + nombreUsuario + "'"));
        Long miembroId = cuenta.getMiembro() == null ? null : cuenta.getMiembro().getId();
        return new UsuarioAutenticado(cuenta.getId(), cuenta.getNombreUsuario(), cuenta.getContrasena(),
                cuenta.getRol(), miembroId);
    }
}
