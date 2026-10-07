package com.max.escuela.services.grupo;

import com.max.escuela.dto.datos.DatosAulaDTO;
import com.max.escuela.dto.datos.DatosCursoDTO;
import com.max.escuela.dto.datos.DatosMaestroDTO;
import com.max.escuela.dto.grupo.GrupoRequestDTO;
import com.max.escuela.dto.grupo.GrupoResponseDTO;
import com.max.escuela.entities.Aula;
import com.max.escuela.entities.Curso;
import com.max.escuela.entities.Grupo;
import com.max.escuela.entities.Maestro;
import com.max.escuela.exceptions.InvalidDataException;
import com.max.escuela.exceptions.RelatedEntityException;
import com.max.escuela.exceptions.NoSuchResourceException;
import com.max.escuela.mapper.GrupoMapper;
import com.max.escuela.repositories.AulaRepository;
import com.max.escuela.repositories.CursoRepository;
import com.max.escuela.repositories.GrupoRepository;
import com.max.escuela.repositories.HorarioRepository;
import com.max.escuela.repositories.InscripcionRepository;
import com.max.escuela.repositories.MaestroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrupoServiceImplTest {

    private static final String MSG_DUPLICADO =
            "Ya existe un grupo con el mismo curso, maestro, aula y periodo";

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private GrupoMapper grupoMapper;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private MaestroRepository maestroRepository;

    @Mock
    private AulaRepository aulaRepository;

    @Mock
    private InscripcionRepository inscripcionRepository;

    @Mock
    private HorarioRepository horarioRepository;

    @InjectMocks
    private GrupoServiceImpl grupoService;

    private Curso curso;
    private Maestro maestro;
    private Aula aula;
    private Grupo grupo;
    private GrupoResponseDTO grupoResponse;

    @BeforeEach
    void setUp() {
        curso = Curso.builder().id(1L).nombre("Matemáticas 6/7").descripcion("Fundamentos de mogging").creditos(67).build();
        maestro = Maestro.builder().id(1L).nombre("Laura").apellidoPaterno("Martínez")
                .apellidoMaterno("Martínez").email("laura@escuela.com").telefono("5551010789").build();
        aula = Aula.builder().id(1L).nombre("Aula 101").capacidad(30).build();

        grupo = Grupo.builder()
                .id(1L).curso(curso).maestro(maestro).aula(aula).periodo("2026-01")
                .build();

        grupoResponse = new GrupoResponseDTO(
                1L,
                new DatosCursoDTO("Matemáticas 6/7", "Fundamentos de mogging", 67),
                new DatosMaestroDTO("Laura Martínez Martínez", "laura@escuela.com", "5551010789"),
                new DatosAulaDTO("Aula 101", 30),
                List.of("Lunes 08:00 - 10:00"),
                "2026-01");
    }

    /** Stubs de búsqueda de curso, maestro y aula (los tres existen). */
    private void stubRelacionesExistentes() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(maestroRepository.findById(1L)).thenReturn(Optional.of(maestro));
        when(aulaRepository.findById(1L)).thenReturn(Optional.of(aula));
    }

    // ---------- listar() / obtenerPorId() ----------

    @Test
    void listar_debeRetornarListaDeGrupos_cuandoExistenRegistros() {
        when(grupoRepository.findAll()).thenReturn(List.of(grupo));
        when(grupoMapper.entidadAResponse(grupo)).thenReturn(grupoResponse);

        assertThat(grupoService.listar()).containsExactly(grupoResponse);
    }

    @Test
    void listar_debeRetornarListaVacia_cuandoNoHayGrupos() {
        when(grupoRepository.findAll()).thenReturn(List.of());

        assertThat(grupoService.listar()).isEmpty();
    }

    @Test
    void obtenerPorId_debeRetornarGrupo_cuandoExiste() {
        when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
        when(grupoMapper.entidadAResponse(grupo)).thenReturn(grupoResponse);

        assertThat(grupoService.obtenerPorId(1L).periodo()).isEqualTo("2026-01");
    }

    @Test
    void obtenerPorId_debeLanzarExcepcion_cuandoNoExiste() {
        when(grupoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> grupoService.obtenerPorId(99L))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Grupo no encontrado con id: 99");
    }

    // ---------- registrar() ----------

    @Test
    void registrar_debeGuardarYRetornarGrupo_cuandoLaCombinacionEsUnica() {
        GrupoRequestDTO request = new GrupoRequestDTO(1L, 1L, 1L, "2026-01");
        stubRelacionesExistentes();
        when(grupoRepository.existsByCursoIdAndMaestroIdAndAulaIdAndPeriodo(1L, 1L, 1L, "2026-01"))
                .thenReturn(false);
        when(grupoMapper.requestAEntidad(request, curso, maestro, aula)).thenReturn(grupo);
        when(grupoMapper.entidadAResponse(grupo)).thenReturn(grupoResponse);

        GrupoResponseDTO resultado = grupoService.registrar(request);

        assertThat(resultado).isEqualTo(grupoResponse);
        verify(grupoRepository).saveAndFlush(grupo);
    }

    @Test
    void registrar_debeLanzarExcepcion_cuandoElCursoNoExiste() {
        GrupoRequestDTO request = new GrupoRequestDTO(99L, 1L, 1L, "2026-01");
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> grupoService.registrar(request))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Curso no encontrado con id: 99");

        verifyNoInteractions(maestroRepository, aulaRepository, grupoMapper);
        verify(grupoRepository, never()).saveAndFlush(any());
    }

    @Test
    void registrar_debeLanzarExcepcion_cuandoElMaestroNoExiste() {
        GrupoRequestDTO request = new GrupoRequestDTO(1L, 99L, 1L, "2026-01");
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(maestroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> grupoService.registrar(request))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Maestro no encontrado con id: 99");

        verifyNoInteractions(aulaRepository, grupoMapper);
        verify(grupoRepository, never()).saveAndFlush(any());
    }

    @Test
    void registrar_debeLanzarExcepcion_cuandoElAulaNoExiste() {
        GrupoRequestDTO request = new GrupoRequestDTO(1L, 1L, 99L, "2026-01");
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(maestroRepository.findById(1L)).thenReturn(Optional.of(maestro));
        when(aulaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> grupoService.registrar(request))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Aula no encontrado con id: 99");

        verifyNoInteractions(grupoMapper);
        verify(grupoRepository, never()).saveAndFlush(any());
    }

    @Test
    void registrar_debeLanzarExcepcion_cuandoYaExisteLaCombinacionCursoMaestroAulaPeriodo() {
        GrupoRequestDTO request = new GrupoRequestDTO(1L, 1L, 1L, "2026-01");
        stubRelacionesExistentes();
        when(grupoRepository.existsByCursoIdAndMaestroIdAndAulaIdAndPeriodo(1L, 1L, 1L, "2026-01"))
                .thenReturn(true);

        assertThatThrownBy(() -> grupoService.registrar(request))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_DUPLICADO);

        verifyNoInteractions(grupoMapper);
        verify(grupoRepository, never()).saveAndFlush(any());
    }

    // ---------- actualizar() ----------

    @Test
    void actualizar_noDebeValidarNiGuardar_cuandoNoHayCambios() {
        GrupoRequestDTO request = new GrupoRequestDTO(1L, 1L, 1L, "2026-01");
        when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
        stubRelacionesExistentes();
        when(grupoMapper.entidadAResponse(grupo)).thenReturn(grupoResponse);

        GrupoResponseDTO resultado = grupoService.actualizar(request, 1L);

        assertThat(resultado).isEqualTo(grupoResponse);
        verify(grupoRepository, never()).existsByCursoIdAndMaestroIdAndAulaIdAndPeriodoAndIdNot(
                anyLong(), anyLong(), anyLong(), anyString(), anyLong());
        verify(grupoRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_debeActualizarYGuardar_cuandoCambiaElPeriodoYLaCombinacionEsUnica() {
        GrupoRequestDTO request = new GrupoRequestDTO(1L, 1L, 1L, "2026-02");
        when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
        stubRelacionesExistentes();
        when(grupoRepository.existsByCursoIdAndMaestroIdAndAulaIdAndPeriodoAndIdNot(1L, 1L, 1L, "2026-02", 1L))
                .thenReturn(false);
        when(grupoMapper.entidadAResponse(grupo)).thenReturn(grupoResponse);

        grupoService.actualizar(request, 1L);

        assertThat(grupo.getPeriodo()).isEqualTo("2026-02");
        verify(grupoRepository).saveAndFlush(grupo);
    }

    @Test
    void actualizar_debeActualizarRelaciones_cuandoCambiaElCurso() {
        Curso otroCurso = Curso.builder().id(2L).nombre("Programación Java").descripcion("Intro").creditos(8).build();
        GrupoRequestDTO request = new GrupoRequestDTO(2L, 1L, 1L, "2026-01");
        when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
        when(cursoRepository.findById(2L)).thenReturn(Optional.of(otroCurso));
        when(maestroRepository.findById(1L)).thenReturn(Optional.of(maestro));
        when(aulaRepository.findById(1L)).thenReturn(Optional.of(aula));
        when(grupoRepository.existsByCursoIdAndMaestroIdAndAulaIdAndPeriodoAndIdNot(2L, 1L, 1L, "2026-01", 1L))
                .thenReturn(false);
        when(grupoMapper.entidadAResponse(grupo)).thenReturn(grupoResponse);

        grupoService.actualizar(request, 1L);

        assertThat(grupo.getCurso()).isSameAs(otroCurso);
        verify(grupoRepository).saveAndFlush(grupo);
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoLaNuevaCombinacionYaExiste() {
        GrupoRequestDTO request = new GrupoRequestDTO(1L, 1L, 1L, "2026-02");
        when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
        stubRelacionesExistentes();
        when(grupoRepository.existsByCursoIdAndMaestroIdAndAulaIdAndPeriodoAndIdNot(1L, 1L, 1L, "2026-02", 1L))
                .thenReturn(true);

        assertThatThrownBy(() -> grupoService.actualizar(request, 1L))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_DUPLICADO);

        assertThat(grupo.getPeriodo()).isEqualTo("2026-01"); // sin cambios
        verify(grupoRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoElPeriodoTieneFormatoInvalido() {
        GrupoRequestDTO request = new GrupoRequestDTO(1L, 1L, 1L, "2026-1");
        when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
        stubRelacionesExistentes();

        assertThatThrownBy(() -> grupoService.actualizar(request, 1L))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("El periodo debe tener el formato YYYY-MM");

        verify(grupoRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoElGrupoNoExiste() {
        GrupoRequestDTO request = new GrupoRequestDTO(1L, 1L, 1L, "2026-01");
        when(grupoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> grupoService.actualizar(request, 99L))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Grupo no encontrado con id: 99");

        verifyNoInteractions(cursoRepository, maestroRepository, aulaRepository);
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoElNuevoMaestroNoExiste() {
        GrupoRequestDTO request = new GrupoRequestDTO(1L, 99L, 1L, "2026-01");
        when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(maestroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> grupoService.actualizar(request, 1L))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Maestro no encontrado con id: 99");

        verify(grupoRepository, never()).saveAndFlush(any());
    }

    // ---------- eliminar() ----------

    @Test
    void eliminar_debeEliminarGrupo_cuandoNoTieneInscripcionesNiHorarios() {
        when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
        when(inscripcionRepository.existsByGrupoId(1L)).thenReturn(false);
        when(horarioRepository.existsByGrupoId(1L)).thenReturn(false);

        grupoService.eliminar(1L);

        verify(grupoRepository).delete(grupo);
        verify(grupoRepository).flush();
    }

    @Test
    void eliminar_debeLanzarExcepcion_cuandoTieneInscripciones() {
        when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
        when(inscripcionRepository.existsByGrupoId(1L)).thenReturn(true);

        assertThatThrownBy(() -> grupoService.eliminar(1L))
                .isInstanceOf(RelatedEntityException.class)
                .hasMessageContaining("inscripciones asociadas");

        verifyNoInteractions(horarioRepository);
        verify(grupoRepository, never()).delete(any());
    }

    @Test
    void eliminar_debeLanzarExcepcion_cuandoTieneHorarios() {
        when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
        when(inscripcionRepository.existsByGrupoId(1L)).thenReturn(false);
        when(horarioRepository.existsByGrupoId(1L)).thenReturn(true);

        assertThatThrownBy(() -> grupoService.eliminar(1L))
                .isInstanceOf(RelatedEntityException.class)
                .hasMessageContaining("horarios asociados");

        verify(grupoRepository, never()).delete(any());
    }

    @Test
    void eliminar_debeLanzarExcepcion_cuandoNoExiste() {
        when(grupoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> grupoService.eliminar(99L))
                .isInstanceOf(NoSuchResourceException.class);

        verifyNoInteractions(inscripcionRepository, horarioRepository);
        verify(grupoRepository, never()).delete(any());
    }
}