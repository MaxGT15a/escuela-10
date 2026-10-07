package com.max.escuela.services.curso;

import com.max.escuela.dto.curso.CursoRequestDTO;
import com.max.escuela.dto.curso.CursoResponseDTO;
import com.max.escuela.entities.Curso;
import com.max.escuela.exceptions.ConflictException;
import com.max.escuela.exceptions.InvalidDataException;
import com.max.escuela.exceptions.RelatedEntityException;
import com.max.escuela.exceptions.NoSuchResourceException;
import com.max.escuela.mapper.CursoMapper;
import com.max.escuela.repositories.CursoRepository;
import com.max.escuela.repositories.GrupoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class CursoServiceImplTest {

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private CursoMapper cursoMapper;

    @Mock
    private GrupoRepository grupoRepository;

    @InjectMocks
    private CursoServiceImpl cursoService;

    private Curso curso;
    private CursoResponseDTO cursoResponse;

    @BeforeEach
    void setUp() {
        curso = Curso.builder()
                .id(1L)
                .nombre("Matemáticas I")
                .descripcion("Fundamentos matemáticos")
                .creditos(6)
                .build();

        cursoResponse = new CursoResponseDTO(1L, "Matemáticas I", "Fundamentos matemáticos", 6);
    }

    // ---------- listar() ----------

    @Test
    void listar_debeRetornarListaDeCursos_cuandoExistenRegistros() {
        when(cursoRepository.findAll()).thenReturn(List.of(curso));
        when(cursoMapper.entidadAResponse(curso)).thenReturn(cursoResponse);

        List<CursoResponseDTO> resultado = cursoService.listar();

        assertThat(resultado).containsExactly(cursoResponse);
        verify(cursoRepository).findAll();
    }

    @Test
    void listar_debeRetornarListaVacia_cuandoNoHayCursosRegistrados() {
        when(cursoRepository.findAll()).thenReturn(List.of());

        assertThat(cursoService.listar()).isEmpty();
    }

    // ---------- obtenerPorId() ----------

    @Test
    void obtenerPorId_debeRetornarCurso_cuandoExiste() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(cursoMapper.entidadAResponse(curso)).thenReturn(cursoResponse);

        CursoResponseDTO resultado = cursoService.obtenerPorId(1L);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.creditos()).isEqualTo(6);
    }

    @Test
    void obtenerPorId_debeLanzarExcepcion_cuandoNoExiste() {
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cursoService.obtenerPorId(99L))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Curso no encontrado con id: 99");
    }

    // ---------- registrar() ----------

    @Test
    void registrar_debeGuardarYRetornarCurso_cuandoElNombreNoExistePreviamente() {
        CursoRequestDTO request = new CursoRequestDTO("  Matemáticas I  ", "  Fundamentos matemáticos ", 6);
        when(cursoRepository.existsByNombre("Matemáticas I")).thenReturn(false);
        when(cursoMapper.entidadAResponse(any(Curso.class))).thenReturn(cursoResponse);

        CursoResponseDTO resultado = cursoService.registrar(request);

        assertThat(resultado).isEqualTo(cursoResponse);
        ArgumentCaptor<Curso> captor = ArgumentCaptor.forClass(Curso.class);
        verify(cursoRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Matemáticas I");
        assertThat(captor.getValue().getDescripcion()).isEqualTo("Fundamentos matemáticos");
        assertThat(captor.getValue().getCreditos()).isEqualTo(6);
    }

    @Test
    void registrar_debeLanzarExcepcion_cuandoYaExisteUnCursoConEseNombre() {
        CursoRequestDTO request = new CursoRequestDTO("Matemáticas I", "Fundamentos matemáticos", 6);
        when(cursoRepository.existsByNombre("Matemáticas I")).thenReturn(true);

        assertThatThrownBy(() -> cursoService.registrar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Nombre del curso ya existente");

        verify(cursoRepository, never()).saveAndFlush(any());
    }

    @Test
    void registrar_debeLanzarExcepcion_cuandoLosDatosSonInvalidos() {
        CursoRequestDTO request = new CursoRequestDTO("Mate", "desc", 6);

        assertThatThrownBy(() -> cursoService.registrar(request))
                .isInstanceOf(InvalidDataException.class);

        verifyNoInteractions(cursoRepository, cursoMapper);
    }

    // ---------- actualizar() ----------

    @Test
    void actualizar_debeActualizarDatos_cuandoCursoExisteYNombreEstaLibre() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(cursoRepository.existsByNombreAndIdNot("Historia I", 1L)).thenReturn(false);

        CursoRequestDTO request = new CursoRequestDTO("Historia I", "Historia universal", 8);
        CursoResponseDTO respuestaEsperada = new CursoResponseDTO(1L, "Historia I", "Historia universal", 8);
        when(cursoMapper.entidadAResponse(curso)).thenReturn(respuestaEsperada);

        CursoResponseDTO resultado = cursoService.actualizar(request, 1L);

        assertThat(resultado.nombre()).isEqualTo("Historia I");
        assertThat(curso.getNombre()).isEqualTo("Historia I");
        assertThat(curso.getDescripcion()).isEqualTo("Historia universal");
        assertThat(curso.getCreditos()).isEqualTo(8);
        verify(cursoRepository).saveAndFlush(curso);
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoElCursoNoExiste() {
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        CursoRequestDTO request = new CursoRequestDTO("Historia I", "Historia universal", 8);

        assertThatThrownBy(() -> cursoService.actualizar(request, 99L))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Curso no encontrado con id: 99");

        verify(cursoRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoElNuevoNombreYaLoUsaOtroCurso() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(cursoRepository.existsByNombreAndIdNot("Historia I", 1L)).thenReturn(true);

        CursoRequestDTO request = new CursoRequestDTO("Historia I", "Historia universal", 8);

        assertThatThrownBy(() -> cursoService.actualizar(request, 1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ya existe un curso con el nombre: Historia I");

        assertThat(curso.getNombre()).isEqualTo("Matemáticas I"); // sin cambios
        verify(cursoRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoLosDatosSonInvalidos() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));

        CursoRequestDTO request = new CursoRequestDTO("Historia I", "desc", 0);

        assertThatThrownBy(() -> cursoService.actualizar(request, 1L))
                .isInstanceOf(InvalidDataException.class);

        verify(cursoRepository, never()).existsByNombreAndIdNot(anyString(), anyLong());
        verify(cursoRepository, never()).saveAndFlush(any());
    }

    // ---------- eliminar() ----------

    @Test
    void eliminar_debeEliminarCurso_cuandoExisteYNoTieneGrupos() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(grupoRepository.existsByCursoId(1L)).thenReturn(false);

        cursoService.eliminar(1L);

        verify(cursoRepository).delete(curso);
        verify(cursoRepository).flush();
    }

    @Test
    void eliminar_debeLanzarExcepcion_cuandoElCursoTieneGruposAsignados() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(grupoRepository.existsByCursoId(1L)).thenReturn(true);

        assertThatThrownBy(() -> cursoService.eliminar(1L))
                .isInstanceOf(RelatedEntityException.class)
                .hasMessageContaining("No se puede eliminar si tiene grupos asignados");

        verify(cursoRepository, never()).delete(any());
    }

    @Test
    void eliminar_debeLanzarExcepcion_cuandoNoExiste() {
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cursoService.eliminar(99L))
                .isInstanceOf(NoSuchResourceException.class);

        verify(cursoRepository, never()).delete(any());
        verifyNoInteractions(grupoRepository);
    }
}