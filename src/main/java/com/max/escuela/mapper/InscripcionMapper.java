package com.max.escuela.mapper;

import com.max.escuela.dto.datos.DatosInscripcionDTO;
import com.max.escuela.dto.inscripcion.InscripcionRequestDTO;
import com.max.escuela.dto.inscripcion.InscripcionResponseDTO;
import com.max.escuela.entities.Alumno;
import com.max.escuela.entities.Grupo;
import com.max.escuela.entities.Inscripcion;
import com.max.escuela.utils.StringCustomUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InscripcionMapper implements CommonMapper<InscripcionRequestDTO, InscripcionResponseDTO, Inscripcion>{
    private final AlumnoMapper alumnoMapper;
    private final GrupoMapper grupoMapper;

    @Override
    public Inscripcion requestAEntidad(InscripcionRequestDTO request) {
        return request == null ? null
                : Inscripcion.crear();
    }
    public Inscripcion requestAEntidad(InscripcionRequestDTO request, Alumno alumno, Grupo grupo) {
        Inscripcion inscripcion = requestAEntidad(request);
        inscripcion.asignarAlumno(alumno);
        inscripcion.asignarGrupo(grupo);
        return inscripcion;
    }

    @Override
    public InscripcionResponseDTO entidadAResponse(Inscripcion inscripcion) {
        return inscripcion == null ? null
                : new InscripcionResponseDTO(
                        inscripcion.getId(),
                        alumnoMapper.entidadADatosAlumno(inscripcion.getAlumno()),
                        grupoMapper.entidadADatosGrupo(inscripcion.getGrupo()),
                        inscripcion.getCalificacion() == null ? null : inscripcion.getCalificacion().getCalificacion(),
                        StringCustomUtils.localDateAString(inscripcion.getFechaInscripcion())
        );
    }

    public DatosInscripcionDTO entidadADatosInscripcion(Inscripcion inscripcion){
        return inscripcion == null ? null
                : new DatosInscripcionDTO(
                alumnoMapper.entidadADatosAlumno(inscripcion.getAlumno()),
                grupoMapper.entidadADatosGrupo(inscripcion.getGrupo()),
                StringCustomUtils.localDateAString(inscripcion.getFechaInscripcion())
        );
    }
}
