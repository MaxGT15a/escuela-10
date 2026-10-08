package com.max.escuela.services.grupo;

import com.max.escuela.dto.grupo.GrupoRequestDTO;
import com.max.escuela.dto.grupo.GrupoResponseDTO;
import com.max.escuela.entities.Aula;
import com.max.escuela.entities.Curso;
import com.max.escuela.entities.Grupo;
import com.max.escuela.entities.Maestro;
import com.max.escuela.exceptions.InvalidDataException;
import com.max.escuela.exceptions.RelatedEntityException;
import com.max.escuela.mapper.GrupoMapper;
import com.max.escuela.repositories.*;
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
public class GrupoServiceImpl implements GrupoService{
    private final GrupoRepository grupoRepository;
    private final GrupoMapper grupoMapper;

    private final CursoRepository cursoRepository;

    private final MaestroRepository maestroRepository;

    private final AulaRepository aulaRepository;

    private final InscripcionRepository inscripcionRepository;
    private final HorarioRepository horarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<GrupoResponseDTO> listar() {
        log.info("Listando grupos");
        return grupoRepository.findAll().stream()
                .map(grupoMapper::entidadAResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GrupoResponseDTO obtenerPorId(Long id) {
        return grupoMapper.entidadAResponse(obtenerGrupo(id));
    }

    @Override
    public GrupoResponseDTO registrar(GrupoRequestDTO request) {
        Curso curso = obtenerCurso(request.idCurso());
        Maestro maestro = obtenerMaestro(request.idMaestro());
        Aula aula = obtenerAula(request.idAula());

        validarDatosUnicos(request);

        Grupo grupo = grupoMapper.requestAEntidad(
                request,
                curso,
                maestro,
                aula
        );

        grupoRepository.saveAndFlush(grupo);
        log.info("Grupo registrado con id: {}", grupo.getId());

        return grupoMapper.entidadAResponse(grupo);
    }

    @Override
    public GrupoResponseDTO actualizar(GrupoRequestDTO request, Long id) {
        Grupo grupo = obtenerGrupo(id);

        Curso curso = obtenerCurso(request.idCurso());
        Maestro maestro = obtenerMaestro(request.idMaestro());
        Aula aula = obtenerAula(request.idAula());

        if (grupo.cambioEnDatos(curso, maestro, aula, request.periodo())) {
            validarCambiosUnicos(request, id);
            grupo.actualizarDatos(
                    curso,
                    maestro,
                    aula,
                    request.periodo()
            );

            grupoRepository.saveAndFlush(grupo);
            log.info("Grupo actualizado con id: {}", grupo.getId());
        }
        return grupoMapper.entidadAResponse(grupo);
    }

    @Override
    public void eliminar(Long id) {
        Grupo grupo = obtenerGrupo(id);

        if(inscripcionRepository.existsByGrupoId(id))
            throw new RelatedEntityException(
                    "No se puede eliminar el grupo con id: " + id + " porque tiene inscripciones asociadas"
            );

        if (horarioRepository.existsByGrupoId(id))
            throw new RelatedEntityException(
                    "No se puede eliminar el grupo con id: " + id + " porque tiene horarios asociados"
            );

        grupoRepository.delete(grupo);
        grupoRepository.flush();
        log.info("Grupo eliminado con id: {}", grupo.getId());
    }

    private Grupo obtenerGrupo(Long id) {
        return ServiceUtils.obtenerEntidadOException(
                grupoRepository,
                id,
                Grupo.class
        );
    }

    private void validarDatosUnicos(GrupoRequestDTO request) {
        if (grupoRepository.existsByCursoIdAndMaestroIdAndAulaIdAndPeriodo(
                request.idCurso(),
                request.idMaestro(),
                request.idAula(),
                request.periodo()
        ))
            throw new InvalidDataException(
                    "Ya existe un grupo con el mismo curso, maestro, aula y periodo"
            );
    }

    private void validarCambiosUnicos(GrupoRequestDTO request, Long id) {
        if (grupoRepository.existsByCursoIdAndMaestroIdAndAulaIdAndPeriodoAndIdNot(
                request.idCurso(),
                request.idMaestro(),
                request.idAula(),
                request.periodo(),
                id
        ))
            throw new InvalidDataException(
                    "Ya existe un grupo con el mismo curso, maestro, aula y periodo"
            );
    }

    private Curso obtenerCurso(Long id){
        return ServiceUtils.obtenerEntidadOException(
                cursoRepository,
                id,
                Curso.class
        );
    }

    private Maestro obtenerMaestro(Long id){
        return ServiceUtils.obtenerEntidadOException(
                maestroRepository,
                id,
                Maestro.class
        );
    }

    private Aula obtenerAula(Long id){
        return ServiceUtils.obtenerEntidadOException(
                aulaRepository,
                id,
                Aula.class
        );
    }
}
