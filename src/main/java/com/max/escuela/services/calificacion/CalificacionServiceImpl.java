package com.max.escuela.services.calificacion;

import com.max.escuela.dto.calificacion.CalificacionRequestDTO;
import com.max.escuela.dto.calificacion.CalificacionResponseDTO;
import com.max.escuela.entities.Calificacion;
import com.max.escuela.entities.Inscripcion;
import com.max.escuela.exceptions.ConflictException;
import com.max.escuela.mapper.CalificacionMapper;
import com.max.escuela.repositories.CalificacionRepository;
import com.max.escuela.repositories.InscripcionRepository;
import com.max.escuela.utils.ServiceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CalificacionServiceImpl implements CalificacionService{
    private final CalificacionRepository calificacionRepository;
    private final CalificacionMapper calificacionMapper;

    private final InscripcionRepository inscripcionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CalificacionResponseDTO> listar() {
        log.info("Listando calificaciones");
        return calificacionRepository.findAll().stream()
                .map(calificacionMapper::entidadAResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CalificacionResponseDTO obtenerPorId(Long id) {
        return calificacionMapper.entidadAResponse(obtenerCalificacion(id));
    }

    @Override
    public CalificacionResponseDTO registrar(CalificacionRequestDTO request) {

        Inscripcion inscripcion = obtenerInscripcion(request.idInscripcion());

        if(calificacionRepository.existsByInscripcionId(request.idInscripcion()))
            throw new ConflictException("La inscripción ya tiene una calificación registrada");

        Calificacion calificacion = calificacionMapper.requestAEntidad(request, inscripcion);

        calificacionRepository.saveAndFlush(calificacion);
        log.info("Calificación registrada con éxito: {}", calificacion.getId());
        return calificacionMapper.entidadAResponse(calificacion);
    }

    @Override
    public CalificacionResponseDTO actualizar(CalificacionRequestDTO request, Long id) {

        Calificacion calificacion = obtenerCalificacion(id);

        calificacion.actualizar(
                request.calificacion()
        );

        calificacionRepository.saveAndFlush(calificacion);
        log.info("Calificación actualizada con éxito: {}", calificacion.getId());
        return calificacionMapper.entidadAResponse(calificacion);
    }

    @Override
    public void eliminar(Long id) {

        Calificacion calificacion = obtenerCalificacion(id);

        calificacionRepository.delete(calificacion);
        calificacionRepository.flush();
        log.info("Calificación eliminada con éxito: {}", calificacion.getId());

    }

    private Calificacion obtenerCalificacion(Long id) {
        return ServiceUtils.obtenerEntidadOException(
                calificacionRepository,
                id,
                Calificacion.class
        );
    }

    private Inscripcion obtenerInscripcion(Long id) {
        return ServiceUtils.obtenerEntidadOException(
                inscripcionRepository,
                id,
                Inscripcion.class
        );
    }
}
