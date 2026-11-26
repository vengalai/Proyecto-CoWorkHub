package com.coworkhub.persistencia.repositorio;

import com.coworkhub.negocio.model.Sede;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositorioSedes extends JpaRepository<Sede, Long> {

    List<Sede> findAllByOrderByNombreAsc();
}
