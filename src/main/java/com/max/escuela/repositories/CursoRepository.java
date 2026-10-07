package com.max.escuela.repositories;

import com.max.escuela.entities.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {
    @Query("""
        SELECT g.curso
        FROM Grupo g
        WHERE g.maestro.id = :idMaestro
    """)
    List<Curso> obtenerCursosPorIdMaestro(
            @Param("idMaestro") Long idMaestro
    );

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Long id);
}
