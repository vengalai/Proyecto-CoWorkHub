package com.coworkhub.persistencia.repositorio;

import com.coworkhub.negocio.model.PlanMembresia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositorioPlanes extends JpaRepository<PlanMembresia, Long> {

    List<PlanMembresia> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
