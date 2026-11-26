package com.coworkhub.persistencia.repositorio;

import com.coworkhub.negocio.model.DetalleReserva;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioDetallesReserva extends JpaRepository<DetalleReserva, Long> {

    boolean existsByServicioId(Long servicioId);
}
