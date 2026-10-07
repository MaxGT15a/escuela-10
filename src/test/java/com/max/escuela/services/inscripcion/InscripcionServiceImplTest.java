package com.max.escuela.services.inscripcion;

import com.max.escuela.dto.datos.DatosAlumnoDTO;
import com.max.escuela.dto.datos.DatosGrupoDTO;
import com.max.escuela.dto.inscripcion.InscripcionRequestDTO;
import com.max.escuela.dto.inscripcion.InscripcionResponseDTO;
import com.max.escuela.entities.Alumno;
import com.max.escuela.entities.Grupo;
import com.max.escuela.entities.Inscripcion;
import com.max.escuela.exceptions.ConflictException;
import com.max.escuela.exceptions.RelatedEntityException;
import com.max.escuela.exceptions.NoSuchResourceException;
import com.max.escuela.mapper.InscripcionMapper;
import com.max.escuela.repositories.AlumnoRepository;
import com.max.escuela.repositories.CalificacionRepository;
import com.max.escuela.repositories.GrupoRepository;
import com.max.escuela.repositories.InscripcionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InscripcionServiceImplTest {

    @Mock
    private InscripcionRepository inscripcionRepository;

    @Mock
    private InscripcionMapper inscripcionMapper;

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private CalificacionRepository calificacionRepository;

    @InjectMocks
    private InscripcionServiceImpl inscripcionService;

    private Alumno alumno;
    private Grupo grupo;
    private Inscripcion inscripcion;
    private InscripcionResponseDTO inscripcionResponse;

    @BeforeEach
    void setUp() {
        alumno = Alumno.builder()
                .id(10L).nombre("Carlos").apellidoPaterno("González").apellidoMaterno("Ramírez")
                .build();

        grupo = Grupo.builder().id(5L).periodo("2026-01").build();

        inscripcion = Inscripcion.builder()
                .id(1L)
                .alumno(alumno)
                .grupo(grupo)
                .fechaInscripcion(LocalDate.of(2026, 2, 11))
                .build();
        alumno.agregarInscripcion(inscripcion);
        grupo.agregarInscripcion(inscripcion);

        inscripcionResponse = new InscripcionResponseDTO(
                1L,
                new DatosAlumnoDTO("Carlos González Ramírez", "GORA260101", "carlos@escuela.com", "10/01/2026"),
                new DatosGrupoDTO("Matemáticas I", "Laura Martínez Martínez", "Aula 101", "2026-01"),
                null,
                "11/02/2026");
    }

    // ---------- listar() / obtenerPorId() ----------

    @Test
    void listar_debeRetornarListaDeInscripciones_cuandoExistenRegistros() {
        when(inscripcionRepository.findAll()).thenReturn(List.of(inscripcion));
        when(inscripcionMapper.entidadAResponse(inscripcion)).thenReturn(inscripcionResponse);

        assertThat(inscripcionService.listar()).containsExactly(inscripcionResponse);
    }

    @Test
    void listar_debeRetornarListaVacia_cuandoNoHayInscripciones() {
        when(inscripcionRepository.findAll()).thenReturn(List.of());

        assertThat(inscripcionService.listar()).isEmpty();
    }

    @Test
    void obtenerPorId_debeRetornarInscripcion_cuandoExiste() {
        when(inscripcionRepository.findById(1L)).thenReturn(Optional.of(inscripcion));
        when(inscripcionMapper.entidadAResponse(inscripcion)).thenReturn(inscripcionResponse);

        assertThat(inscripcionService.obtenerPorId(1L).id()).isEqualTo(1L);
    }

    @Test
    void obtenerPorId_debeLanzarExcepcion_cuandoNoExiste() {
        when(inscripcionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inscripcionService.obtenerPorId(99L))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Inscripcion no encontrado con id: 99");
    }

    // ---------- registrar() ----------

    @Test
    void registrar_debeGuardarYRetornarInscripcion_cuandoAlumnoYGrupoExistenYNoEstabaInscrito() {
        InscripcionRequestDTO request = new InscripcionRequestDTO(10L, 5L);
        when(alumnoRepository.findById(10L)).thenReturn(Optional.of(alumno));
        when(grupoRepository.findById(5L)).thenReturn(Optional.of(grupo));
        when(inscripcionRepository.existsByAlumnoIdAndGrupoId(10L, 5L)).thenReturn(false);
        when(inscripcionMapper.requestAEntidad(request, alumno, grupo)).thenReturn(inscripcion);
        when(inscripcionMapper.entidadAResponse(inscripcion)).thenReturn(inscripcionResponse);

        InscripcionResponseDTO resultado = inscripcionService.registrar(request);

        assertThat(resultado).isEqualTo(inscripcionResponse);
        verify(inscripcionRepository).saveAndFlush(inscripcion);
    }

    @Test
    void registrar_debeLanzarExcepcion_cuandoElAlumnoNoExiste() {
        InscripcionRequestDTO request = new InscripcionRequestDTO(99L, 5L);
        when(alumnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inscripcionService.registrar(request))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Alumno no encontrado con id: 99");

        verifyNoInteractions(grupoRepository, inscripcionMapper);
        verify(inscripcionRepository, never()).saveAndFlush(any());
    }

    @Test
    void registrar_debeLanzarExcepcion_cuandoElGrupoNoExiste() {
        InscripcionRequestDTO request = new InscripcionRequestDTO(10L, 99L);
        when(alumnoRepository.findById(10L)).thenReturn(Optional.of(alumno));
        when(grupoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inscripcionService.registrar(request))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Grupo no encontrado con id: 99");

        verify(inscripcionRepository, never()).saveAndFlush(any());
    }

    @Test
    void registrar_debeLanzarExcepcion_cuandoElAlumnoYaEstaInscritoEnElGrupo() {
        InscripcionRequestDTO request = new InscripcionRequestDTO(10L, 5L);
        when(alumnoRepository.findById(10L)).thenReturn(Optional.of(alumno));
        when(grupoRepository.findById(5L)).thenReturn(Optional.of(grupo));
        when(inscripcionRepository.existsByAlumnoIdAndGrupoId(10L, 5L)).thenReturn(true);

        assertThatThrownBy(() -> inscripcionService.registrar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("El alumno ya está inscrito en este grupo");

        verifyNoInteractions(inscripcionMapper);
        verify(inscripcionRepository, never()).saveAndFlush(any());
    }

    // ---------- actualizar() ----------

    @Test
    void actualizar_noDebeValidarNiGuardar_cuandoAlumnoYGrupoSonLosMismos() {
        InscripcionRequestDTO request = new InscripcionRequestDTO(10L, 5L);
        when(inscripcionRepository.findById(1L)).thenReturn(Optional.of(inscripcion));
        when(alumnoRepository.findById(10L)).thenReturn(Optional.of(alumno));
        when(grupoRepository.findById(5L)).thenReturn(Optional.of(grupo));
        when(inscripcionMapper.entidadAResponse(inscripcion)).thenReturn(inscripcionResponse);

        InscripcionResponseDTO resultado = inscripcionService.actualizar(request, 1L);

        assertThat(resultado).isEqualTo(inscripcionResponse);
        verify(inscripcionRepository, never())
                .existsByAlumnoIdAndGrupoIdAndIdNot(anyLong(), anyLong(), anyLong());
        verify(inscripcionRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_debeMoverLaInscripcionYGuardar_cuandoCambiaElAlumno() {
        Alumno otroAlumno = Alumno.builder()
                .id(11L).nombre("Marcos").apellidoPaterno("Pérez").apellidoMaterno("Salgado")
                .build();
        InscripcionRequestDTO request = new InscripcionRequestDTO(11L, 5L);
        when(inscripcionRepository.findById(1L)).thenReturn(Optional.of(inscripcion));
        when(alumnoRepository.findById(11L)).thenReturn(Optional.of(otroAlumno));
        when(grupoRepository.findById(5L)).thenReturn(Optional.of(grupo));
        when(inscripcionRepository.existsByAlumnoIdAndGrupoIdAndIdNot(11L, 5L, 1L)).thenReturn(false);
        when(inscripcionMapper.entidadAResponse(inscripcion)).thenReturn(inscripcionResponse);

        inscripcionService.actualizar(request, 1L);

        assertThat(inscripcion.getAlumno()).isSameAs(otroAlumno);
        assertThat(alumno.getInscripciones()).isEmpty();
        assertThat(otroAlumno.getInscripciones()).containsExactly(inscripcion);
        verify(inscripcionRepository).saveAndFlush(inscripcion);
    }

    @Test
    void actualizar_debeMoverLaInscripcionYGuardar_cuandoCambiaElGrupo() {
        Grupo otroGrupo = Grupo.builder().id(6L).periodo("2026-02").build();
        InscripcionRequestDTO request = new InscripcionRequestDTO(10L, 6L);
        when(inscripcionRepository.findById(1L)).thenReturn(Optional.of(inscripcion));
        when(alumnoRepository.findById(10L)).thenReturn(Optional.of(alumno));
        when(grupoRepository.findById(6L)).thenReturn(Optional.of(otroGrupo));
        when(inscripcionRepository.existsByAlumnoIdAndGrupoIdAndIdNot(10L, 6L, 1L)).thenReturn(false);
        when(inscripcionMapper.entidadAResponse(inscripcion)).thenReturn(inscripcionResponse);

        inscripcionService.actualizar(request, 1L);

        assertThat(inscripcion.getGrupo()).isSameAs(otroGrupo);
        assertThat(grupo.getInscripciones()).isEmpty();
        assertThat(otroGrupo.getInscripciones()).containsExactly(inscripcion);
        verify(inscripcionRepository).saveAndFlush(inscripcion);
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoLaNuevaCombinacionYaExiste() {
        Grupo otroGrupo = Grupo.builder().id(6L).periodo("2026-02").build();
        InscripcionRequestDTO request = new InscripcionRequestDTO(10L, 6L);
        when(inscripcionRepository.findById(1L)).thenReturn(Optional.of(inscripcion));
        when(alumnoRepository.findById(10L)).thenReturn(Optional.of(alumno));
        when(grupoRepository.findById(6L)).thenReturn(Optional.of(otroGrupo));
        when(inscripcionRepository.existsByAlumnoIdAndGrupoIdAndIdNot(10L, 6L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> inscripcionService.actualizar(request, 1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("El alumno ya está inscrito en este grupo");

        assertThat(inscripcion.getGrupo()).isSameAs(grupo); // sin cambios
        verify(inscripcionRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoLaInscripcionNoExiste() {
        InscripcionRequestDTO request = new InscripcionRequestDTO(10L, 5L);
        when(inscripcionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inscripcionService.actualizar(request, 99L))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Inscripcion no encontrado con id: 99");

        verifyNoInteractions(alumnoRepository, grupoRepository);
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoElNuevoAlumnoNoExiste() {
        InscripcionRequestDTO request = new InscripcionRequestDTO(99L, 5L);
        when(inscripcionRepository.findById(1L)).thenReturn(Optional.of(inscripcion));
        when(alumnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inscripcionService.actualizar(request, 1L))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Alumno no encontrado con id: 99");

        verify(inscripcionRepository, never()).saveAndFlush(any());
    }

    // ---------- eliminar() ----------

    @Test
    void eliminar_debeEliminarInscripcion_cuandoNoTieneCalificaciones() {
        when(inscripcionRepository.findById(1L)).thenReturn(Optional.of(inscripcion));
        when(calificacionRepository.existsByInscripcionId(1L)).thenReturn(false);

        inscripcionService.eliminar(1L);

        verify(inscripcionRepository).delete(inscripcion);
    }

    @Test
    void eliminar_debeLanzarExcepcion_cuandoTieneCalificacionesAsociadas() {
        when(inscripcionRepository.findById(1L)).thenReturn(Optional.of(inscripcion));
        when(calificacionRepository.existsByInscripcionId(1L)).thenReturn(true);

        assertThatThrownBy(() -> inscripcionService.eliminar(1L))
                .isInstanceOf(RelatedEntityException.class)
                .hasMessageContaining("calificaciones asociadas");

        verify(inscripcionRepository, never()).delete(any());
    }

    @Test
    void eliminar_debeLanzarExcepcion_cuandoNoExiste() {
        when(inscripcionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inscripcionService.eliminar(99L))
                .isInstanceOf(NoSuchResourceException.class);

        verify(inscripcionRepository, never()).delete(any());
        verifyNoInteractions(calificacionRepository);
    }
}