package com.max.escuela.services.aula;

import com.max.escuela.dto.aula.AulaRequestDTO;
import com.max.escuela.dto.aula.AulaResponseDTO;
import com.max.escuela.entities.Aula;
import com.max.escuela.exceptions.ConflictException;
import com.max.escuela.exceptions.RelatedEntityException;
import com.max.escuela.mapper.AulaMapper;
import com.max.escuela.repositories.AulaRepository;
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
public class AulaServiceImpl implements AulaService{

    private final AulaRepository aulaRepository;
    private final AulaMapper aulaMapper;

    private final GrupoRepository grupoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AulaResponseDTO> listar() {
        log.info("Listando aulas");
        return aulaRepository.findAll().stream()
                .map(aulaMapper::entidadAResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AulaResponseDTO obtenerPorId(Long id) {
        return aulaMapper.entidadAResponse(obtenerAula(id));
    }

    @Override
    public AulaResponseDTO registrar(AulaRequestDTO request) {
        validarDatosUnicos(request.nombre());

        Aula aula = aulaMapper.requestAEntidad(request);

        aulaRepository.saveAndFlush(aula);

        log.info("Aula registrada con id: {}", aula.getId());

        return aulaMapper.entidadAResponse(aula);
    }

    @Override
    public AulaResponseDTO actualizar(AulaRequestDTO request, Long id) {
        Aula aula = obtenerAula(id);

        validarCambiosUnicos(request.nombre(), id);

        Aula aulaConCambios = aulaMapper.requestAEntidad(request);

        aula.actualizar(
                aulaConCambios.getNombre(),
                aulaConCambios.getCapacidad()
        );

        aulaRepository.saveAndFlush(aula);

        log.info("Aula actualizada con id: {}", aula.getId());

        return aulaMapper.entidadAResponse(aula);
    }

    @Override
    public void eliminar(Long id) {
        Aula aula = obtenerAula(id);

        // Validar si el aula tiene grupos asignados antes de eliminarlo
        if(grupoRepository.existsByAulaId(id))
            throw new RelatedEntityException("No se puede eliminar si tiene grupos asignados");

        aulaRepository.delete(aula);

        aulaRepository.flush();

        log.info("Aula eliminada con id: {}", aula.getId());
    }

    private Aula obtenerAula(Long id){
        return ServiceUtils.obtenerEntidadOException(
                aulaRepository,
                id,
                Aula.class
        );
    }

    private void validarDatosUnicos(String nombre){
        if(aulaRepository.existsByNombre(nombre)){
            throw new ConflictException("El nombre del aula ya existe");
        }
    }

    private void validarCambiosUnicos(String nombre, Long id){
        if(aulaRepository.existsByNombreAndIdNot(nombre, id)){
            throw new ConflictException("Una aula con el mismo nombre ya existe");
        }
    }
}
