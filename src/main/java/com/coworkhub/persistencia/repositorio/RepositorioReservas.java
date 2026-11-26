package com.coworkhub.persistencia.repositorio;

import com.coworkhub.negocio.model.EstadoReserva;
import com.coworkhub.negocio.model.Reserva;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RepositorioReservas extends JpaRepository<Reserva, Long> {

    @EntityGraph(attributePaths = {"sala", "sala.sede", "miembro", "detalles", "detalles.servicio"})
    @Query("select r from Reserva r where r.id = :id")
    Optional<Reserva> buscarCompleta(@Param("id") Long id);

    boolean existsBySalaIdAndEstadoInAndInicioLessThanAndFinGreaterThan(
            Long salaId, Collection<EstadoReserva> estados, LocalDateTime fin, LocalDateTime inicio);

    long countByMiembroIdAndEstadoInAndInicioAfter(
            Long miembroId, Collection<EstadoReserva> estados, LocalDateTime instante);

    @Query("""
            select coalesce(sum(r.minutosConsumidos), 0)
            from Reserva r
            where r.miembro.id = :miembroId
              and r.inicio >= :desde
              and r.inicio < :hasta
            """)
    long sumarMinutosConsumidos(@Param("miembroId") Long miembroId,
                                @Param("desde") LocalDateTime desde,
                                @Param("hasta") LocalDateTime hasta);

    boolean existsBySalaId(Long salaId);

    boolean existsByMiembroId(Long miembroId);

    boolean existsByIdAndMiembroId(Long id, Long miembroId);

    @EntityGraph(attributePaths = {"sala", "sala.sede", "miembro", "detalles", "detalles.servicio"})
    List<Reserva> findByMiembroIdOrderByInicioDesc(Long miembroId);

    @EntityGraph(attributePaths = {"sala", "sala.sede", "miembro", "detalles", "detalles.servicio"})
    List<Reserva> findByEstadoOrderByInicioAsc(EstadoReserva estado);

    @EntityGraph(attributePaths = {"sala", "sala.sede", "miembro", "detalles", "detalles.servicio"})
    List<Reserva> findBySalaIdAndInicioBetweenOrderByInicioAsc(
            Long salaId, LocalDateTime desde, LocalDateTime hasta);
}
