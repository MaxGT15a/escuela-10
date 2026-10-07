package com.max.escuela.mapper;

import com.max.escuela.dto.datos.DatosGrupoDTO;
import com.max.escuela.dto.grupo.GrupoRequestDTO;
import com.max.escuela.dto.grupo.GrupoResponseDTO;
import com.max.escuela.entities.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrupoMapper implements CommonMapper<GrupoRequestDTO, GrupoResponseDTO, Grupo>{
    private final CursoMapper cursoMapper;
    private final MaestroMapper maestroMapper;
    private final AulaMapper aulaMapper;

    @Override
    public Grupo requestAEntidad(GrupoRequestDTO request) {
        return request == null ? null
                : Grupo.crear(
                request.periodo()
        );
    }

    public Grupo requestAEntidad(GrupoRequestDTO request, Curso curso, Maestro maestro, Aula aula) {
        Grupo grupo = requestAEntidad(request);

        grupo.asignarCurso(curso);
        grupo.asignarMaestro(maestro);
        grupo.asignarAula(aula);

        return grupo;
    }

    @Override
    public GrupoResponseDTO entidadAResponse(Grupo grupo) {
        return grupo == null ? null
                : new GrupoResponseDTO(
                grupo.getId(),
                cursoMapper.entidadADatosCurso(grupo.getCurso()),
                maestroMapper.entidadADatosMaestro(grupo.getMaestro()),
                aulaMapper.entidadADatosAula(grupo.getAula()),
                grupo.getHorarios().stream()
                        .map(Horario::obtenerHorarioCompleto)
                        .toList(),
                grupo.getPeriodo()
        );
    }

    public DatosGrupoDTO entidadADatosGrupo(Grupo grupo){
        return grupo == null ? null
                : new DatosGrupoDTO(
                grupo.getCurso().getNombre(),
                grupo.getMaestro().obtenerNombreCompleto(),
                grupo.getAula().getNombre(),
                grupo.getPeriodo()
        );
    }
}
