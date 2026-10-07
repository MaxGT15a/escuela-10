package com.max.escuela.mapper;

import com.max.escuela.dto.calificacion.CalificacionRequestDTO;
import com.max.escuela.dto.calificacion.CalificacionResponseDTO;
import com.max.escuela.entities.Calificacion;
import com.max.escuela.entities.Inscripcion;
import com.max.escuela.utils.StringCustomUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CalificacionMapper implements CommonMapper<CalificacionRequestDTO, CalificacionResponseDTO, Calificacion>{
    private final InscripcionMapper inscripcionMapper;

    @Override
    public Calificacion requestAEntidad(CalificacionRequestDTO request) {
        return request == null ? null
                : Calificacion.crear(
                request.calificacion()
        );
    }
    public Calificacion requestAEntidad(CalificacionRequestDTO request, Inscripcion inscripcion) {
        Calificacion calificacion = requestAEntidad(request);
        calificacion.asignarInscripcion(inscripcion);
        return calificacion;
    }

    @Override
    public CalificacionResponseDTO entidadAResponse(Calificacion calificacion) {
        return calificacion == null ? null
                : new CalificacionResponseDTO(
                calificacion.getId(),
                inscripcionMapper.entidadADatosInscripcion(calificacion.getInscripcion()),
                calificacion.getCalificacion(),
                StringCustomUtils.localDateAString(
                        calificacion.getInscripcion().getFechaInscripcion()
                )
        );
    }
}
