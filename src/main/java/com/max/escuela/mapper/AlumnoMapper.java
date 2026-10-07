package com.max.escuela.mapper;

import com.max.escuela.dto.alumno.AlumnoRequestDTO;
import com.max.escuela.dto.alumno.AlumnoResponseDTO;
import com.max.escuela.dto.datos.DatosAlumnoDTO;
import com.max.escuela.dto.datos.DatosCalificacionDTO;
import com.max.escuela.entities.Alumno;
import com.max.escuela.utils.StringCustomUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlumnoMapper implements CommonMapper<AlumnoRequestDTO, AlumnoResponseDTO, Alumno> {

    @Override
    public Alumno requestAEntidad(AlumnoRequestDTO request) {
        return request == null ? null
                : Alumno.crear(
                request.nombre(),
                request.apellidoPaterno(),
                request.apellidoMaterno()
        );
    }

    public Alumno requestAEntidad(AlumnoRequestDTO request, String email, String matricula) {
        if(request == null) return null;

        Alumno alumno = requestAEntidad(request);

        alumno.asignarDatosAcademicos(email, matricula);

        return alumno;
    }

    @Override
    public AlumnoResponseDTO entidadAResponse(Alumno alumno) {
        if (alumno == null) return null;

        return new AlumnoResponseDTO(
                alumno.getId(),
                String.join(" ",
                        alumno.getNombre(),
                        alumno.getApellidoPaterno(),
                        alumno.getApellidoMaterno()
                ),
                alumno.getEmail(),
                alumno.getMatricula(),
                StringCustomUtils.localDateAString(alumno.getFechaIngreso()),
                entidadADatosCalificacion(alumno),
                alumno.calcularPromedio()
        );
    }

    public List<DatosCalificacionDTO> entidadADatosCalificacion(Alumno alumno) {
        if (alumno == null) return List.of();

        return alumno.getInscripciones().stream()
                .map(inscripcion -> new DatosCalificacionDTO(
                        inscripcion.getGrupo().getCurso().getNombre(),
                        inscripcion.getGrupo().getPeriodo(),
                        inscripcion.getCalificacion() != null ?
                                inscripcion.getCalificacion().getCalificacion()
                                : null
                ))
                .toList();
    }

    public DatosAlumnoDTO entidadADatosAlumno(Alumno alumno){
        return alumno == null ? null
                : new DatosAlumnoDTO(
                        alumno.obtenerNombreCompleto(),
                        alumno.getMatricula(),
                        alumno.getEmail(),
                        StringCustomUtils.localDateAString(alumno.getFechaIngreso())
        );
    }
}