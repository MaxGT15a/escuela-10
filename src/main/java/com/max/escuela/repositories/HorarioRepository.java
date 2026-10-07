package com.max.escuela.repositories;

import com.max.escuela.entities.Horario;
import com.max.escuela.enums.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HorarioRepository extends JpaRepository<Horario, Long> {

    /**
     * Traslape: inicioExistente < finNuevo  &&  finExistente > inicioNuevo
     * (ver diagrama de casos en la documentación del proyecto)
     */
    @Query("""
        select count(h) > 0
        from Horario h
        where h.diaSemana = :dia
          and h.horaInicio < :horaFin
          and h.horaFin > :horaInicio
          and h.grupo.periodo = :periodo
          and (h.grupo.id = :idGrupo or h.grupo.aula.id = :idAula)
          and h.id <> :idExcluir
        """)
    boolean existeTraslape(
            @Param("dia") DiaSemana dia,
            @Param("horaInicio") String horaInicio,
            @Param("horaFin") String horaFin,
            @Param("periodo") String periodo,
            @Param("idGrupo") Long idGrupo,
            @Param("idAula") Long idAula,
            @Param("idExcluir") Long idExcluir   // -1L al registrar
    );

    boolean existsByGrupoId(Long grupoId);
}
