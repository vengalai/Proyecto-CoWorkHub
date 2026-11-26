package com.coworkhub.persistencia.repositorio;

import com.coworkhub.negocio.model.EstadoReserva;
import com.coworkhub.negocio.model.Sala;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RepositorioSalas extends JpaRepository<Sala, Long> {

    @EntityGraph(attributePaths = {"sede", "equipamientos"})
    List<Sala> findAllByOrderByNombreAsc();

    @EntityGraph(attributePaths = {"sede", "equipamientos"})
    List<Sala> findBySedeIdOrderByNombreAsc(Long sedeId);

    @EntityGraph(attributePaths = {"sede", "equipamientos"})
    @Query("select s from Sala s where s.id = :id")
    Optional<Sala> buscarCompleta(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Sala s where s.id = :id")
    Optional<Sala> buscarParaActualizar(@Param("id") Long id);

    boolean existsBySedeId(Long sedeId);

    boolean existsBySedeIdAndNombreIgnoreCase(Long sedeId, String nombre);

    boolean existsBySedeIdAndNombreIgnoreCaseAndIdNot(Long sedeId, String nombre, Long id);

    boolean existsByEquipamientosId(Long equipamientoId);

    @EntityGraph(attributePaths = {"sede", "equipamientos"})
    @Query("""
            select s from Sala s
            where s.sede.id = :sedeId
              and s.activa = true
              and s.capacidad >= :capacidad
              and not exists (
                  select r from Reserva r
                  where r.sala = s
                    and r.estado in :estados
                    and r.inicio < :fin
                    and r.fin > :inicio
              )
            order by s.nombre
            """)
    List<Sala> buscarDisponibles(@Param("sedeId") Long sedeId,
                                 @Param("capacidad") int capacidad,
                                 @Param("inicio") LocalDateTime inicio,
                                 @Param("fin") LocalDateTime fin,
                                 @Param("estados") Collection<EstadoReserva> estados);
}
