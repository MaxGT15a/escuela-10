package com.max.escuela.services.horario;

import com.max.escuela.dto.horario.HorarioRequestDTO;
import com.max.escuela.dto.horario.HorarioResponseDTO;
import com.max.escuela.entities.Grupo;
import com.max.escuela.entities.Horario;
import com.max.escuela.enums.DiaSemana;
import com.max.escuela.exceptions.RelatedEntityException;
import com.max.escuela.mapper.HorarioMapper;
import com.max.escuela.repositories.GrupoRepository;
import com.max.escuela.repositories.HorarioRepository;
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
public class HorarioServiceImpl implements HorarioService{
    private final HorarioRepository horarioRepository;
    private final HorarioMapper horarioMapper;

    private final GrupoRepository grupoRepository;

    @Override
    public List<HorarioResponseDTO> listar() {
        log.info("Listando horarios");
        return horarioRepository.findAll().stream()
                .map(horarioMapper::entidadAResponse).toList();
    }

    @Override
    public HorarioResponseDTO obtenerPorId(Long id) {
        return horarioMapper.entidadAResponse(obtenerHorario(id));
    }

    @Override
    public HorarioResponseDTO registrar(HorarioRequestDTO request) {
        Grupo grupo = obtenerGrupo(request.idGrupo());
        DiaSemana dia = DiaSemana.obtenerDiaPorDescripcion(request.dia());

        validarHorario(grupo, dia, request.horaInicio(), request.horaFin(), -1L);

        Horario horario = horarioMapper.requestAEntidad(request, grupo);
        horarioRepository.saveAndFlush(horario);
        log.info("Horario registrado con id: {}", horario.getId());
        return horarioMapper.entidadAResponse(horario);
    }

    @Override
    public HorarioResponseDTO actualizar(HorarioRequestDTO request, Long id) {
        Horario horario = obtenerHorario(id);
        Grupo grupo = obtenerGrupo(request.idGrupo());
        DiaSemana dia = DiaSemana.obtenerDiaPorDescripcion(request.dia());

        if (horario.cambioEnDatos(request.dia(), request.horaInicio(), request.horaFin(), grupo)) {
            validarHorario(grupo, dia, request.horaInicio(), request.horaFin(), id);
            horario.actualizar(request.dia(), request.horaInicio(), request.horaFin(), grupo);
            horarioRepository.saveAndFlush(horario);
            log.info("Horario actualizado con id: {}", id);
        }
        return horarioMapper.entidadAResponse(horario);
    }

    @Override
    public void eliminar(Long id) {
        Horario horario = obtenerHorario(id);
        horarioRepository.delete(horario);
        horarioRepository.flush();
        log.info("Horario eliminado con id: {}", horario.getId());
    }

    private Horario obtenerHorario(Long id){
        return ServiceUtils.obtenerEntidadOException(
                horarioRepository,
                id,
                Horario.class
        );
    }

    private Grupo obtenerGrupo(Long id) {
        return ServiceUtils.obtenerEntidadOException(
                grupoRepository,
                id,
                Grupo.class
        );
    }

    private void validarHorario(
            Grupo grupo,
            DiaSemana dia,
            String horaInicio,
            String horaFin,
            Long idExcluir
    ) {
        if (horarioRepository.existeTraslape(dia, horaInicio, horaFin,
                grupo.getPeriodo(), grupo.getId(), grupo.getAula().getId(), idExcluir))
            throw new RelatedEntityException("El horario se traslapa con otro del mismo grupo o aula");
    }
}
