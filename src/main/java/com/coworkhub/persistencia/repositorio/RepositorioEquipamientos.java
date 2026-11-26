package com.coworkhub.persistencia.repositorio;

import com.coworkhub.negocio.model.Equipamiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositorioEquipamientos extends JpaRepository<Equipamiento, Long> {

    List<Equipamiento> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
