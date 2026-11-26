package com.coworkhub.persistencia.repositorio;

import com.coworkhub.negocio.model.ServicioAdicional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositorioServiciosAdicionales extends JpaRepository<ServicioAdicional, Long> {

    List<ServicioAdicional> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
