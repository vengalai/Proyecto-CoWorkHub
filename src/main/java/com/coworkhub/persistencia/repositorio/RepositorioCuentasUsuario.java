package com.coworkhub.persistencia.repositorio;

import com.coworkhub.negocio.model.CuentaUsuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepositorioCuentasUsuario extends JpaRepository<CuentaUsuario, Long> {

    @EntityGraph(attributePaths = "miembro")
    Optional<CuentaUsuario> findByNombreUsuarioIgnoreCase(String nombreUsuario);

    boolean existsByNombreUsuarioIgnoreCase(String nombreUsuario);

    Optional<CuentaUsuario> findByMiembroId(Long miembroId);

    boolean existsByMiembroId(Long miembroId);
}
