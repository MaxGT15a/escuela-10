package com.max.escuela.mapper;

import com.max.escuela.dto.curso.CursoRequestDTO;
import com.max.escuela.dto.curso.CursoResponseDTO;
import com.max.escuela.dto.datos.DatosCursoDTO;
import com.max.escuela.entities.Curso;
import com.max.escuela.exceptions.InvalidDataException;
import org.springframework.stereotype.Component;

@Component
public class CursoMapper implements CommonMapper<CursoRequestDTO, CursoResponseDTO, Curso>{
    @Override
    public Curso requestAEntidad(CursoRequestDTO request) {
        if (request == null) {
            throw new InvalidDataException("El request de curso es nulo");
        }
        return Curso.crear(
                request.nombre(),
                request.descripcion(),
                request.creditos()
        );
    }

    @Override
    public CursoResponseDTO entidadAResponse(Curso curso) {
        return curso == null ? null
                : new CursoResponseDTO(
                curso.getId(),
                curso.getNombre(),
                curso.getDescripcion(),
                curso.getCreditos()
        );
    }

    public DatosCursoDTO entidadADatosCurso(Curso curso){
        return curso == null ? null
                : new DatosCursoDTO(
                    curso.getNombre(),
                    curso.getDescripcion(),
                    curso.getCreditos()
        );
    }
}
