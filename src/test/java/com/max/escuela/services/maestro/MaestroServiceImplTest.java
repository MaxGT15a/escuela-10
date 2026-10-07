package com.max.escuela.services.maestro;

import com.max.escuela.dto.datos.DatosCursoDTO;
import com.max.escuela.dto.maestro.MaestroRequestDTO;
import com.max.escuela.dto.maestro.MaestroResponseDTO;
import com.max.escuela.entities.Curso;
import com.max.escuela.entities.Maestro;
import com.max.escuela.exceptions.ConflictException;
import com.max.escuela.exceptions.InvalidDataException;
import com.max.escuela.exceptions.RelatedEntityException;
import com.max.escuela.exceptions.NoSuchResourceException;
import com.max.escuela.mapper.CursoMapper;
import com.max.escuela.mapper.MaestroMapper;
import com.max.escuela.repositories.CursoRepository;
import com.max.escuela.repositories.GrupoRepository;
import com.max.escuela.repositories.MaestroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
class MaestroServiceImplTest {

    @Mock
    private MaestroRepository maestroRepository;

    @Mock
    private MaestroMapper maestroMapper;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private CursoMapper cursoMapper;

    @Mock
    private GrupoRepository grupoRepository;

    @InjectMocks
    private MaestroServiceImpl maestroService;

    private Maestro maestro;
    private MaestroResponseDTO maestroResponse;

    @BeforeEach
    void setUp() {
        maestro = Maestro.builder()
                .id(1L)
                .nombre("Laura")
                .apellidoPaterno("Martínez")
                .apellidoMaterno("López")
                .email("laura@escuela.com")
                .telefono("5551010789")
                .build();

        maestroResponse = new MaestroResponseDTO(
                1L, "Laura Martínez López", "laura@escuela.com", "5551010789", List.of());
    }

    // ---------- listar() ----------
    @Test
    void listar_debeRetornarListaDeMaestros_cuandoExistenRegistros() {
        when(maestroRepository.findAll()).thenReturn(List.of(maestro));
        when(maestroMapper.entidadAResponse(maestro)).thenReturn(maestroResponse);

        List<MaestroResponseDTO> resultado = maestroService.listar();

        assertThat(resultado).containsExactly(maestroResponse);
        verify(maestroRepository).findAll();
    }

    @Test
    void listar_debeRetornarListaVacia_cuandoNoHayMaestrosRegistrados() {
        when(maestroRepository.findAll()).thenReturn(List.of());

        assertThat(maestroService.listar()).isEmpty();
    }

    // ---------- obtenerPorId() ----------
    @Test
    void obtenerPorId_debeRetornarMaestro_cuandoExiste() {
        when(maestroRepository.findById(1L)).thenReturn(Optional.of(maestro));
        when(maestroMapper.entidadAResponse(maestro)).thenReturn(maestroResponse);

        MaestroResponseDTO resultado = maestroService.obtenerPorId(1L);

        assertThat(resultado.id()).isEqualTo(1L);
    }

    @Test
    void obtenerPorId_debeLanzarExcepcion_cuandoNoExiste() {
        when(maestroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> maestroService.obtenerPorId(99L))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Maestro no encontrado con id: 99");
    }

    // ---------- registrar() ----------
    @Test
    void registrar_debeGuardarYRetornarMaestro_cuandoEmailYTelefonoEstanLibres() {
        MaestroRequestDTO request = new MaestroRequestDTO(
                "Laura", "Martínez", "López", "laura@escuela.com", "5551010789");
        when(maestroRepository.existsByEmail("laura@escuela.com")).thenReturn(false);
        when(maestroRepository.existsByTelefono("5551010789")).thenReturn(false);
        when(maestroMapper.entidadAResponse(any(Maestro.class))).thenReturn(maestroResponse);

        MaestroResponseDTO resultado = maestroService.registrar(request);

        assertThat(resultado).isEqualTo(maestroResponse);
        verify(maestroRepository).saveAndFlush(any(Maestro.class));
    }

    @Test
    void registrar_debeValidarUnicidadConEmailNormalizado() {
        // Maestro.crear recorta y pasa a minúsculas; el service debe consultar con ese valor
        MaestroRequestDTO request = new MaestroRequestDTO(
                "Laura", "Martínez", "López", "  Laura@Escuela.COM ", "5551010789");
        when(maestroRepository.existsByEmail("laura@escuela.com")).thenReturn(false);
        when(maestroRepository.existsByTelefono("5551010789")).thenReturn(false);
        when(maestroMapper.entidadAResponse(any(Maestro.class))).thenReturn(maestroResponse);

        maestroService.registrar(request);

        ArgumentCaptor<Maestro> captor = ArgumentCaptor.forClass(Maestro.class);
        verify(maestroRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("laura@escuela.com");
    }

    @Test
    void registrar_debeLanzarExcepcion_cuandoElEmailYaExiste() {
        MaestroRequestDTO request = new MaestroRequestDTO(
                "Laura", "Martínez", "López", "laura@escuela.com", "5551010789");
        when(maestroRepository.existsByEmail("laura@escuela.com")).thenReturn(true);

        assertThatThrownBy(() -> maestroService.registrar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email ya existente");

        verify(maestroRepository, never()).saveAndFlush(any());
    }

    @Test
    void registrar_debeLanzarExcepcion_cuandoElTelefonoYaExiste() {
        MaestroRequestDTO request = new MaestroRequestDTO(
                "Laura", "Martínez", "López", "laura@escuela.com", "5551010789");
        when(maestroRepository.existsByEmail("laura@escuela.com")).thenReturn(false);
        when(maestroRepository.existsByTelefono("5551010789")).thenReturn(true);

        assertThatThrownBy(() -> maestroService.registrar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Telefono ya existente");

        verify(maestroRepository, never()).saveAndFlush(any());
    }

    @Test
    void registrar_debeLanzarExcepcion_cuandoLosDatosSonInvalidos() {
        MaestroRequestDTO request = new MaestroRequestDTO(
                "Ana", "Martínez", "López", "laura@escuela.com", "5551010789");

        assertThatThrownBy(() -> maestroService.registrar(request))
                .isInstanceOf(InvalidDataException.class);

        verifyNoInteractions(maestroRepository, maestroMapper);
    }

    // ---------- actualizar() ----------
    @Test
    void actualizar_debeActualizarDatos_cuandoMaestroExisteYDatosUnicosEstanLibres() {
        when(maestroRepository.findById(1L)).thenReturn(Optional.of(maestro));
        when(maestroRepository.existsByEmailAndIdNot("karla@escuela.com", 1L)).thenReturn(false);
        when(maestroRepository.existsByTelefonoAndIdNot("5559999999", 1L)).thenReturn(false);

        MaestroRequestDTO request = new MaestroRequestDTO(
                "Karla", "Gómez", "Pérez", "karla@escuela.com", "5559999999");
        MaestroResponseDTO respuestaEsperada = new MaestroResponseDTO(
                1L, "Karla Gómez Pérez", "karla@escuela.com", "5559999999", List.of());
        when(maestroMapper.entidadAResponse(maestro)).thenReturn(respuestaEsperada);

        MaestroResponseDTO resultado = maestroService.actualizar(request, 1L);

        assertThat(resultado.nombre()).isEqualTo("Karla Gómez Pérez");
        assertThat(maestro.getNombre()).isEqualTo("Karla");
        assertThat(maestro.getEmail()).isEqualTo("karla@escuela.com");
        assertThat(maestro.getTelefono()).isEqualTo("5559999999");
        verify(maestroRepository).saveAndFlush(maestro);
    }

    @Test
    void actualizar_noDebeValidarNiGuardar_cuandoNoHayCambios() {
        when(maestroRepository.findById(1L)).thenReturn(Optional.of(maestro));
        when(maestroMapper.entidadAResponse(maestro)).thenReturn(maestroResponse);

        MaestroRequestDTO request = new MaestroRequestDTO(
                "Laura", "Martínez", "López", "laura@escuela.com", "5551010789");

        MaestroResponseDTO resultado = maestroService.actualizar(request, 1L);

        assertThat(resultado).isEqualTo(maestroResponse);
        verify(maestroRepository, never()).existsByEmailAndIdNot(anyString(), anyLong());
        verify(maestroRepository, never()).existsByTelefonoAndIdNot(anyString(), anyLong());
        verify(maestroRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoElMaestroNoExiste() {
        when(maestroRepository.findById(99L)).thenReturn(Optional.empty());

        MaestroRequestDTO request = new MaestroRequestDTO(
                "Karla", "Gómez", "Pérez", "karla@escuela.com", "5559999999");

        assertThatThrownBy(() -> maestroService.actualizar(request, 99L))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Maestro no encontrado con id: 99");

        verify(maestroRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoElNuevoEmailYaLoUsaOtroMaestro() {
        when(maestroRepository.findById(1L)).thenReturn(Optional.of(maestro));
        when(maestroRepository.existsByEmailAndIdNot("otro@escuela.com", 1L)).thenReturn(true);

        MaestroRequestDTO request = new MaestroRequestDTO(
                "Karla", "Gómez", "Pérez", "otro@escuela.com", "5559999999");

        assertThatThrownBy(() -> maestroService.actualizar(request, 1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Otro maestro ya tiene este email");

        verify(maestroRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoElNuevoTelefonoYaLoUsaOtroMaestro() {
        when(maestroRepository.findById(1L)).thenReturn(Optional.of(maestro));
        when(maestroRepository.existsByEmailAndIdNot("karla@escuela.com", 1L)).thenReturn(false);
        when(maestroRepository.existsByTelefonoAndIdNot("5559999999", 1L)).thenReturn(true);

        MaestroRequestDTO request = new MaestroRequestDTO(
                "Karla", "Gómez", "Pérez", "karla@escuela.com", "5559999999");

        assertThatThrownBy(() -> maestroService.actualizar(request, 1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Otro maestro ya tiene este telefono");

        assertThat(maestro.getNombre()).isEqualTo("Laura"); // sin cambios
        verify(maestroRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoLosDatosSonInvalidos() {
        when(maestroRepository.findById(1L)).thenReturn(Optional.of(maestro));

        MaestroRequestDTO request = new MaestroRequestDTO(
                "Karla", "Gómez", "Pérez", "karla@escuela.com", "123");

        assertThatThrownBy(() -> maestroService.actualizar(request, 1L))
                .isInstanceOf(InvalidDataException.class);

        verify(maestroRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_debeValidarUnicidadConEmailNormalizado() {
        when(maestroRepository.findById(1L)).thenReturn(Optional.of(maestro));
        when(maestroRepository.existsByEmailAndIdNot("karla@escuela.com", 1L)).thenReturn(false);
        when(maestroRepository.existsByTelefonoAndIdNot("5559999999", 1L)).thenReturn(false);
        when(maestroMapper.entidadAResponse(maestro)).thenReturn(maestroResponse);

        MaestroRequestDTO request = new MaestroRequestDTO(
                "Karla", "Gómez", "Pérez", "  KARLA@Escuela.com ", "5559999999");

        maestroService.actualizar(request, 1L);

        verify(maestroRepository).existsByEmailAndIdNot("karla@escuela.com", 1L);
        verify(maestroRepository).saveAndFlush(maestro);
    }

    // ---------- eliminar() ----------
    @Test
    void eliminar_debeEliminarMaestro_cuandoExisteYNoTieneGruposAsignados() {
        when(maestroRepository.findById(1L)).thenReturn(Optional.of(maestro));
        when(grupoRepository.existsByMaestroId(1L)).thenReturn(false);

        maestroService.eliminar(1L);

        verify(maestroRepository).delete(maestro);
        verify(maestroRepository).flush();
    }

    @Test
    void eliminar_debeLanzarExcepcion_cuandoElMaestroTieneGruposAsignados() {
        when(maestroRepository.findById(1L)).thenReturn(Optional.of(maestro));
        when(grupoRepository.existsByMaestroId(1L)).thenReturn(true);

        assertThatThrownBy(() -> maestroService.eliminar(1L))
                .isInstanceOf(RelatedEntityException.class)
                .hasMessageContaining("No se puede eliminar si tiene grupos asignados");

        verify(maestroRepository, never()).delete(any());
    }

    @Test
    void eliminar_debeLanzarExcepcion_cuandoNoExiste() {
        when(maestroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> maestroService.eliminar(99L))
                .isInstanceOf(NoSuchResourceException.class);

        verify(maestroRepository, never()).delete(any());
        verifyNoInteractions(grupoRepository);
    }

    // ---------- obtenerCursosDeUnMaestroConId() ----------
    @Test
    void obtenerCursosDeUnMaestroConId_debeRetornarCursos_cuandoElMaestroExiste() {
        Curso curso = Curso.builder()
                .id(1L).nombre("Matemáticas I").descripcion("Fundamentos").creditos(6)
                .build();
        DatosCursoDTO datosCurso = new DatosCursoDTO("Matemáticas I", "Fundamentos", 6);

        when(maestroRepository.existsById(1L)).thenReturn(true);
        when(cursoRepository.obtenerCursosPorIdMaestro(1L)).thenReturn(List.of(curso));
        when(cursoMapper.entidadADatosCurso(curso)).thenReturn(datosCurso);

        List<DatosCursoDTO> resultado = maestroService.obtenerCursosDeUnMaestroConId(1L);

        assertThat(resultado).containsExactly(datosCurso);
    }

    @Test
    void obtenerCursosDeUnMaestroConId_debeRetornarListaVacia_cuandoNoTieneCursos() {
        when(maestroRepository.existsById(1L)).thenReturn(true);
        when(cursoRepository.obtenerCursosPorIdMaestro(1L)).thenReturn(List.of());

        assertThat(maestroService.obtenerCursosDeUnMaestroConId(1L)).isEmpty();
    }

    @Test
    void obtenerCursosDeUnMaestroConId_debeLanzarNoEncontrado_cuandoElMaestroNoExiste() {
        when(maestroRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> maestroService.obtenerCursosDeUnMaestroConId(99L))
                .isInstanceOf(NoSuchResourceException.class);
    }
}