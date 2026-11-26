package com.coworkhub.persistencia.repositorio;

import com.coworkhub.negocio.model.EstadoMiembro;
import com.coworkhub.negocio.model.Miembro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepositorioMiembros extends JpaRepository<Miembro, Long> {

    @EntityGraph(attributePaths = {"plan", "cuenta"})
    @Query("""
            select m from Miembro m
            where (:nombre is null or lower(m.nombreCompleto) like lower(concat('%', :nombre, '%')))
              and (:estado is null or m.estado = :estado)
            """)
    Page<Miembro> buscar(@Param("nombre") String nombre,
                         @Param("estado") EstadoMiembro estado,
                         Pageable pageable);

    @EntityGraph(attributePaths = {"plan", "cuenta"})
    @Query("select m from Miembro m where m.id = :id")
    Optional<Miembro> buscarCompleto(@Param("id") Long id);

    @EntityGraph(attributePaths = {"plan", "cuenta"})
    List<Miembro> findByEstadoOrderByNombreCompletoAsc(EstadoMiembro estado);

    boolean existsByNumeroDocumentoIgnoreCase(String numeroDocumento);

    boolean existsByNumeroDocumentoIgnoreCaseAndIdNot(String numeroDocumento, Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByPlanId(Long planId);
}
