package com.max.escuela.services.maestro;

import com.max.escuela.dto.datos.DatosCursoDTO;
import com.max.escuela.dto.maestro.MaestroRequestDTO;
import com.max.escuela.dto.maestro.MaestroResponseDTO;
import com.max.escuela.entities.Maestro;
import com.max.escuela.exceptions.ConflictException;
import com.max.escuela.exceptions.NoSuchResourceException;
import com.max.escuela.exceptions.RelatedEntityException;
import com.max.escuela.mapper.CursoMapper;
import com.max.escuela.mapper.MaestroMapper;
import com.max.escuela.repositories.CursoRepository;
import com.max.escuela.repositories.GrupoRepository;
import com.max.escuela.repositories.MaestroRepository;
import com.max.escuela.utils.ServiceUtils;
import com.max.escuela.utils.StringCustomUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MaestroServiceImpl implements MaestroService{

    private final MaestroRepository maestroRepository;
    private final MaestroMapper maestroMapper;

    private final CursoRepository cursoRepository;
    private final CursoMapper cursoMapper;

    private final GrupoRepository grupoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MaestroResponseDTO> listar() {
        log.info("Listando maestros");
        return maestroRepository.findAll().stream()
                .map(maestroMapper::entidadAResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MaestroResponseDTO obtenerPorId(Long id) {
        return maestroMapper.entidadAResponse(obterMaestro(id));
    }

    @Override
    public MaestroResponseDTO registrar(MaestroRequestDTO request) {

        Maestro maestro = Maestro.crear(
                request.nombre(),
                request.apellidoPaterno(),
                request.apellidoMaterno(),
                request.email(),
                request.telefono()
        );

        validarDatosUnicos(maestro.getEmail(), maestro.getTelefono());

        maestroRepository.saveAndFlush(maestro);
        log.info("Maestro agregado con id: {}", maestro.getId());
        return maestroMapper.entidadAResponse(maestro);
    }

    @Override
    public MaestroResponseDTO actualizar(MaestroRequestDTO request, Long id) {
        Maestro maestro = obterMaestro(id);

        if(maestro.cambioEnDatos(
                request.nombre(),
                request.apellidoPaterno(),
                request.apellidoMaterno(),
                request.email(),
                request.telefono()
        )) {
            validarCambiosUnicos(StringCustomUtils.normalizarTexto(request.email()),
                    request.telefono(), id);
            maestro.actualizar(
                    request.nombre(),
                    request.apellidoPaterno(),
                    request.apellidoMaterno(),
                    StringCustomUtils.normalizarTexto(request.email()),
                    request.telefono()
            );

            maestroRepository.saveAndFlush(maestro);

            log.info("Maestro {} actualizado con id: {}", maestro.getNombre(), maestro.getId());
        }

        return maestroMapper.entidadAResponse(maestro);
    }

    @Override
    public void eliminar(Long id) {
        Maestro maestro = obterMaestro(id);

        // Validar si el maestro tiene grupos asignados antes de eliminarlo
        if(grupoRepository.existsByMaestroId(id))
            throw new RelatedEntityException("No se puede eliminar si tiene grupos asignados");

        maestroRepository.delete(maestro);
        maestroRepository.flush();

        log.info("Maestro eliminado con id: {}", id);
    }

    @Transactional(readOnly = true)
    public List<DatosCursoDTO> obtenerCursosDeUnMaestroConId(Long id){
        if(!maestroRepository.existsById(id))
            throw new NoSuchResourceException("El maestro no existe con id: " + id);

        log.info("Listando cursos del maestro con id: {}", id);

        return cursoRepository.obtenerCursosPorIdMaestro(id).stream()
                .map(cursoMapper::entidadADatosCurso).toList();
    }

    private Maestro obterMaestro(Long id){
        return ServiceUtils.obtenerEntidadOException(
                maestroRepository,
                id,
                Maestro.class
        );
    }

    private void validarDatosUnicos(String email, String telefono){
        if(maestroRepository.existsByEmail(email))
            throw new ConflictException("Email ya existente");

        if(maestroRepository.existsByTelefono(telefono))
            throw new ConflictException("Telefono ya existente");
    }

    private void validarCambiosUnicos(String email, String telefono, Long id){
        if(maestroRepository.existsByEmailAndIdNot(email, id))
            throw new ConflictException("Otro maestro ya tiene este email");

        if(maestroRepository.existsByTelefonoAndIdNot(telefono, id))
            throw new ConflictException("Otro maestro ya tiene este telefono");
    }
}
