package com.max.escuela.mapper;

import com.max.escuela.dto.datos.DatosMaestroDTO;
import com.max.escuela.dto.maestro.MaestroRequestDTO;
import com.max.escuela.dto.maestro.MaestroResponseDTO;
import com.max.escuela.entities.Grupo;
import com.max.escuela.entities.Maestro;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaestroMapper implements CommonMapper<MaestroRequestDTO, MaestroResponseDTO, Maestro>{

    private final CursoMapper cursoMapper;

    @Override
    public Maestro requestAEntidad(MaestroRequestDTO request) {
        return request == null ? null
            : Maestro.crear(
                request.nombre(),
                request.apellidoPaterno(),
                request.apellidoMaterno(),
                request.email(),
                request.telefono()
        );
    }

    @Override
    public MaestroResponseDTO entidadAResponse(Maestro maestro) {
        return maestro == null ? null
            : new MaestroResponseDTO(
                maestro.getId(),
                String.join(" ",
                    maestro.getNombre(),
                    maestro.getApellidoPaterno(),
                    maestro.getApellidoMaterno()
                ),
                maestro.getEmail(),
                maestro.getTelefono(),
                maestro.getGrupos().stream()
                        .map(Grupo::getCurso)
                        .map(cursoMapper::entidadADatosCurso).toList()
        );
    }

    public DatosMaestroDTO entidadADatosMaestro(Maestro maestro){
        return maestro == null ? null
                : new DatosMaestroDTO(
                String.join(" ",
                        maestro.getNombre(),
                        maestro.getApellidoPaterno(),
                        maestro.getApellidoMaterno()
                ),
                maestro.getEmail(),
                maestro.getTelefono()
        );
    }
}
