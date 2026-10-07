package com.max.escuela.services.curso;

import com.max.escuela.dto.curso.CursoRequestDTO;
import com.max.escuela.dto.curso.CursoResponseDTO;
import com.max.escuela.entities.Curso;
import com.max.escuela.exceptions.ConflictException;
import com.max.escuela.exceptions.RelatedEntityException;
import com.max.escuela.mapper.CursoMapper;
import com.max.escuela.repositories.CursoRepository;
import com.max.escuela.repositories.GrupoRepository;
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
public class CursoServiceImpl implements CursoService {

    private final CursoRepository cursoRepository;
    private final CursoMapper cursoMapper;

    private final GrupoRepository grupoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CursoResponseDTO> listar() {
        log.info("Listando cursos");
        return cursoRepository.findAll().stream()
                .map(cursoMapper::entidadAResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CursoResponseDTO obtenerPorId(Long id) {
        return cursoMapper.entidadAResponse(obtenerCurso(id));
    }

    @Override
    public CursoResponseDTO registrar(CursoRequestDTO request) {

        Curso curso = Curso.crear(
                request.nombre(),
                request.descripcion(),
                request.creditos()
        );

        validarDatosUnicos(curso.getNombre());

        cursoRepository.saveAndFlush(curso);

        log.info("Curso registrado con id: {}", curso.getId());

        return cursoMapper.entidadAResponse(curso);
    }

    @Override
    public CursoResponseDTO actualizar(CursoRequestDTO request, Long id) {

        Curso curso = obtenerCurso(id);

        Curso cursoConCambios = Curso.crear(
                request.nombre(),
                request.descripcion(),
                request.creditos()
        );

        validarCambiosUnicos(cursoConCambios.getNombre(), id);

        curso.actualizar(
                cursoConCambios.getNombre(),
                cursoConCambios.getDescripcion(),
                cursoConCambios.getCreditos()
        );

        cursoRepository.saveAndFlush(curso);

        log.info("Curso actualizado con id: {}", curso.getId());

        return cursoMapper.entidadAResponse(curso);
    }

    @Override
    public void eliminar(Long id) {
        Curso curso = obtenerCurso(id);

        // Validar si el curso tiene grupos asignados antes de eliminarlo
        if(grupoRepository.existsByCursoId(id))
            throw new RelatedEntityException("No se puede eliminar si tiene grupos asignados");

        cursoRepository.delete(curso);

        cursoRepository.flush();

        log.info("Curso eliminado con id: {}", curso.getId());
    }

    private Curso obtenerCurso(Long id){
        return ServiceUtils.obtenerEntidadOException(
                cursoRepository,
                id,
                Curso.class
        );
    }

    private void validarDatosUnicos(String nombre){
        if(cursoRepository.existsByNombre(nombre))
            throw new ConflictException("Nombre del curso ya existente");
    }

    private void validarCambiosUnicos(String nombre, Long id){
        if(cursoRepository.existsByNombreAndIdNot(nombre, id))
            throw new ConflictException("Ya existe un curso con el nombre: " + nombre);
    }
}
