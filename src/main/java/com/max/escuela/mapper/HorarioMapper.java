package com.max.escuela.mapper;

import com.max.escuela.dto.horario.HorarioRequestDTO;
import com.max.escuela.dto.horario.HorarioResponseDTO;
import com.max.escuela.entities.Grupo;
import com.max.escuela.entities.Horario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HorarioMapper implements CommonMapper<HorarioRequestDTO, HorarioResponseDTO, Horario> {
    private final GrupoMapper grupoMapper;
    @Override
    public Horario requestAEntidad(HorarioRequestDTO request) {
        return request == null ? null
                : Horario.crear(
                        request.dia(),
                        request.horaInicio(),
                        request.horaFin()
        );
    }

    public Horario requestAEntidad(HorarioRequestDTO request, Grupo grupo) {
        Horario horario = requestAEntidad(request);
        horario.asignarGrupo(grupo);
        return horario;
    }

    @Override
    public HorarioResponseDTO entidadAResponse(Horario horario) {
        return horario == null ? null
                : new HorarioResponseDTO(
                        horario.getId(),
                        grupoMapper.entidadADatosGrupo(horario.getGrupo()),
                        horario.obtenerHorarioCompleto()
        );
    }
}
