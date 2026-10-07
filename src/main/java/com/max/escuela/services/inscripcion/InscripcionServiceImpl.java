package com.max.escuela.services.inscripcion;

import com.max.escuela.dto.inscripcion.InscripcionRequestDTO;
import com.max.escuela.dto.inscripcion.InscripcionResponseDTO;
import com.max.escuela.entities.Alumno;
import com.max.escuela.entities.Grupo;
import com.max.escuela.entities.Inscripcion;
import com.max.escuela.exceptions.ConflictException;
import com.max.escuela.exceptions.RelatedEntityException;
import com.max.escuela.mapper.InscripcionMapper;
import com.max.escuela.repositories.AlumnoRepository;
import com.max.escuela.repositories.CalificacionRepository;
import com.max.escuela.repositories.GrupoRepository;
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
public class InscripcionServiceImpl implements InscripcionService{
    private final InscripcionRepository inscripcionRepository;
    private final InscripcionMapper inscripcionMapper;

    private final AlumnoRepository alumnoRepository;
    private final GrupoRepository grupoRepository;
    private final CalificacionRepository calificacionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InscripcionResponseDTO> listar() {
        log.info("Listando inscripciones");
        return inscripcionRepository.findAll().stream()
                .map(inscripcionMapper::entidadAResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InscripcionResponseDTO obtenerPorId(Long id) {
        return inscripcionMapper.entidadAResponse(obtenerInscripcion(id));
    }

    @Override
    public InscripcionResponseDTO registrar(InscripcionRequestDTO request) {
        Alumno alumno = obtenerAlumno(request.idAlumno());
        Grupo grupo = obtenerGrupo(request.idGrupo());

        validarDatosUnicos(request);

        Inscripcion inscripcion = inscripcionMapper.requestAEntidad(request, alumno, grupo);

        inscripcionRepository.saveAndFlush(inscripcion);
        log.info("Inscripción registrada con éxito: {}", inscripcion.getId());
        return inscripcionMapper.entidadAResponse(inscripcion);
    }

    @Override
    public InscripcionResponseDTO actualizar(InscripcionRequestDTO request, Long id) {
        Inscripcion inscripcion = obtenerInscripcion(id);
        Alumno alumno = obtenerAlumno(request.idAlumno());
        Grupo grupo = obtenerGrupo(request.idGrupo());

        if(inscripcion.cambioEnDatos(alumno, grupo)) {
            validarCambiosUnicos(request, id);

            inscripcion.actualizar(
                    alumno,
                    grupo
            );
            inscripcionRepository.saveAndFlush(inscripcion);
            log.info("Inscripción actualizada con éxito: {}", inscripcion.getId());
        }

        return inscripcionMapper.entidadAResponse(inscripcion);
    }

    @Override
    public void eliminar(Long id) {
        Inscripcion inscripcion = obtenerInscripcion(id);

        if(calificacionRepository.existsByInscripcionId(inscripcion.getId()))
            throw new RelatedEntityException("No se puede eliminar la inscripción, ya que tiene calificaciones asociadas");


        inscripcionRepository.delete(inscripcion);
        log.info("Inscripción eliminada con éxito: {}", inscripcion.getId());
    }

    private Inscripcion obtenerInscripcion(Long id) {
        return ServiceUtils.obtenerEntidadOException(
                inscripcionRepository,
                id,
                Inscripcion.class
        );
    }

    private Alumno obtenerAlumno(Long id){
        return ServiceUtils.obtenerEntidadOException(
                alumnoRepository,
                id,
                Alumno.class
        );
    }

    private Grupo obtenerGrupo(Long id) {
        return ServiceUtils.obtenerEntidadOException(
                grupoRepository,
                id,
                Grupo.class
        );
    }

    private void validarDatosUnicos(InscripcionRequestDTO request) {
        if(inscripcionRepository.existsByAlumnoIdAndGrupoId(request.idAlumno(), request.idGrupo()))
            throw new ConflictException("El alumno ya está inscrito en este grupo");
    }

    private void validarCambiosUnicos(InscripcionRequestDTO request, Long id) {
        if(inscripcionRepository.existsByAlumnoIdAndGrupoIdAndIdNot(request.idAlumno(), request.idGrupo(), id))
            throw new ConflictException("El alumno ya está inscrito en este grupo");
    }
}
