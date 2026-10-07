package com.max.escuela.repositories;

import com.max.escuela.entities.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {
    @Query(value = "SELECT GENERAR_MATRICULA(:nombre, :paterno, :materno) FROM DUAL", nativeQuery = true)
    String generarMatricula(@Param("nombre") String nombre,
                            @Param("paterno") String paterno,
                            @Param("materno") String materno);

    @Query(value = "SELECT GENERAR_EMAIL(:nombre, :paterno, :materno) FROM DUAL", nativeQuery = true)
    String generarEmail(@Param("nombre") String nombre,
                        @Param("paterno") String paterno,
                        @Param("materno") String materno);
}
