package com.max.escuela.services.alumno;

import com.max.escuela.dto.alumno.AlumnoRequestDTO;
import com.max.escuela.dto.alumno.AlumnoResponseDTO;
import com.max.escuela.entities.Alumno;
import com.max.escuela.exceptions.InvalidDataException;
import com.max.escuela.exceptions.RelatedEntityException;
import com.max.escuela.exceptions.NoSuchResourceException;
import com.max.escuela.mapper.AlumnoMapper;
import com.max.escuela.repositories.AlumnoRepository;
import com.max.escuela.repositories.InscripcionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlumnoServiceImplTest {

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private AlumnoMapper alumnoMapper;

    @Mock
    private InscripcionRepository inscripcionRepository;

    @InjectMocks
    private AlumnoServiceImpl alumnoService;

    private Alumno alumno;
    private AlumnoResponseDTO alumnoResponse;

    @BeforeEach
    void setUp() {
        alumno = Alumno.builder()
                .id(1L)
                .nombre("Carlos")
                .apellidoPaterno("González")
                .apellidoMaterno("Ramírez")
                .email("tr.2026.carlos.gonzalez.ramirez.gora260101@escuela.com.mx")
                .matricula("GORA260101")
                .build();

        alumnoResponse = new AlumnoResponseDTO(
                1L, "Carlos González Ramírez", alumno.getEmail(), "GORA260101",
                "10/01/2026", List.of(), new BigDecimal("0.00"));
    }

    // ---------- listar() ----------

    @Test
    void listar_debeRetornarListaDeAlumnos_cuandoExistenRegistros() {
        when(alumnoRepository.findAll()).thenReturn(List.of(alumno));
        when(alumnoMapper.entidadAResponse(alumno)).thenReturn(alumnoResponse);

        List<AlumnoResponseDTO> resultado = alumnoService.listar();

        assertThat(resultado).containsExactly(alumnoResponse);
    }

    @Test
    void listar_debeRetornarListaVacia_cuandoNoHayAlumnos() {
        when(alumnoRepository.findAll()).thenReturn(List.of());

        assertThat(alumnoService.listar()).isEmpty();
    }

    // ---------- obtenerPorId() ----------

    @Test
    void obtenerPorId_debeRetornarAlumno_cuandoExiste() {
        when(alumnoRepository.findById(1L)).thenReturn(Optional.of(alumno));
        when(alumnoMapper.entidadAResponse(alumno)).thenReturn(alumnoResponse);

        AlumnoResponseDTO resultado = alumnoService.obtenerPorId(1L);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.matricula()).isEqualTo("GORA260101");
    }

    @Test
    void obtenerPorId_debeLanzarExcepcion_cuandoNoExiste() {
        when(alumnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alumnoService.obtenerPorId(99L))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Alumno no encontrado con id: 99");
    }

    // ---------- registrar() ----------

    @Test
    void registrar_debeGenerarEmailYMatriculaConDatosRecortados_yGuardar() {
        AlumnoRequestDTO request = new AlumnoRequestDTO("  Carlos ", " González", "Ramírez  ");
        when(alumnoRepository.generarEmail("Carlos", "González", "Ramírez")).thenReturn("email@escuela.com.mx");
        when(alumnoRepository.generarMatricula("Carlos", "González", "Ramírez")).thenReturn("GORA260101");
        when(alumnoMapper.requestAEntidad(request, "email@escuela.com.mx", "GORA260101")).thenReturn(alumno);
        when(alumnoMapper.entidadAResponse(alumno)).thenReturn(alumnoResponse);

        AlumnoResponseDTO resultado = alumnoService.registrar(request);

        assertThat(resultado).isEqualTo(alumnoResponse);
        verify(alumnoRepository).saveAndFlush(alumno);
    }

    // ---------- actualizar() ----------

    @Test
    void actualizar_debeRegenerarEmailYMatriculaYGuardar_cuandoCambianLosDatosPersonales() {
        AlumnoRequestDTO request = new AlumnoRequestDTO("Marcos", "Pérez", "Salgado");
        when(alumnoRepository.findById(1L)).thenReturn(Optional.of(alumno));
        when(alumnoRepository.generarEmail("Marcos", "Pérez", "Salgado")).thenReturn("Marcos@Escuela.com.mx");
        when(alumnoRepository.generarMatricula("Marcos", "Pérez", "Salgado")).thenReturn("PESA260102");
        when(alumnoMapper.entidadAResponse(alumno)).thenReturn(alumnoResponse);

        alumnoService.actualizar(request, 1L);

        assertThat(alumno.getNombre()).isEqualTo("Marcos");
        assertThat(alumno.getApellidoPaterno()).isEqualTo("Pérez");
        assertThat(alumno.getApellidoMaterno()).isEqualTo("Salgado");
        assertThat(alumno.getEmail()).isEqualTo("marcos@escuela.com.mx"); // normalizado a minúsculas
        assertThat(alumno.getMatricula()).isEqualTo("PESA260102");
        verify(alumnoRepository).saveAndFlush(alumno);
    }

    @Test
    void actualizar_noDebeRegenerarNiGuardar_cuandoNoCambianLosDatos() {
        AlumnoRequestDTO request = new AlumnoRequestDTO("Carlos", "González", "Ramírez");
        when(alumnoRepository.findById(1L)).thenReturn(Optional.of(alumno));
        when(alumnoMapper.entidadAResponse(alumno)).thenReturn(alumnoResponse);

        AlumnoResponseDTO resultado = alumnoService.actualizar(request, 1L);

        assertThat(resultado).isEqualTo(alumnoResponse);
        assertThat(alumno.getMatricula()).isEqualTo("GORA260101");
        verify(alumnoRepository, never()).generarEmail(anyString(), anyString(), anyString());
        verify(alumnoRepository, never()).generarMatricula(anyString(), anyString(), anyString());
        verify(alumnoRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_noDebeRegenerarNiGuardar_cuandoSoloCambianEspaciosAlrededor() {
        AlumnoRequestDTO request = new AlumnoRequestDTO("  Carlos  ", "González ", " Ramírez");
        when(alumnoRepository.findById(1L)).thenReturn(Optional.of(alumno));
        when(alumnoMapper.entidadAResponse(alumno)).thenReturn(alumnoResponse);

        alumnoService.actualizar(request, 1L);

        verify(alumnoRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoElAlumnoNoExiste() {
        when(alumnoRepository.findById(99L)).thenReturn(Optional.empty());

        AlumnoRequestDTO request = new AlumnoRequestDTO("Marcos", "Pérez", "Salgado");

        assertThatThrownBy(() -> alumnoService.actualizar(request, 99L))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Alumno no encontrado con id: 99");

        verify(alumnoRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoLosDatosSonInvalidos() {
        when(alumnoRepository.findById(1L)).thenReturn(Optional.of(alumno));

        AlumnoRequestDTO request = new AlumnoRequestDTO("Ana", "Pérez", "Salgado");

        assertThatThrownBy(() -> alumnoService.actualizar(request, 1L))
                .isInstanceOf(InvalidDataException.class);

        verify(alumnoRepository, never()).saveAndFlush(any());
    }

    // ---------- eliminar() ----------

    @Test
    void eliminar_debeEliminarAlumno_cuandoNoTieneInscripciones() {
        when(alumnoRepository.findById(1L)).thenReturn(Optional.of(alumno));
        when(inscripcionRepository.existsByAlumnoId(1L)).thenReturn(false);

        alumnoService.eliminar(1L);

        verify(alumnoRepository).delete(alumno);
        verify(alumnoRepository).flush();
    }

    @Test
    void eliminar_debeLanzarExcepcion_cuandoTieneInscripciones() {
        when(alumnoRepository.findById(1L)).thenReturn(Optional.of(alumno));
        when(inscripcionRepository.existsByAlumnoId(1L)).thenReturn(true);

        assertThatThrownBy(() -> alumnoService.eliminar(1L))
                .isInstanceOf(RelatedEntityException.class)
                .hasMessageContaining("No se puede eliminar el alumno con id: 1")
                .hasMessageContaining("inscripciones asociadas");

        verify(alumnoRepository, never()).delete(any());
    }

    @Test
    void eliminar_debeLanzarExcepcion_cuandoNoExiste() {
        when(alumnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alumnoService.eliminar(99L))
                .isInstanceOf(NoSuchResourceException.class);

        verify(alumnoRepository, never()).delete(any());
        verifyNoInteractions(inscripcionRepository);
    }
}